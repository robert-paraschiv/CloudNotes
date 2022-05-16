const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase)
admin.firestore().settings({ timestampsInSnapshots: true })


exports.notesCollaboratorsListener = functions.firestore.document("Users/{userID}/Notes/{noteID}").onCreate((snap, context) => {
    const userID = context.params.userID;
    const noteID = context.params.noteID;

    const newValue = snap.data();

    return admin.firestore().collection("Users").doc(userID).get().then(document => {
        const user_email = document.data().email;
        const note_creator_email = newValue.creator_user_email;
        const user_tokenID = document.data().user_device_token;

        if (user_tokenID === null) {
            return console.log("User token is null");
        } else {
            if (user_email !== note_creator_email) {
                const options = {
                    priority: 'high'
                }
                const payload = {
                    data: {
                        noteID: noteID,
                        title: "New note",
                        body: "You have been added as a collaborator to a new note!",
                        icon: "default",
                        click_action: "com.rokudoz.onotes.CollaboratorNotification"
                    }
                };
                return admin.messaging().sendToDevice(user_tokenID, payload, options).then(result => {
                    return console.log("Notification sent");
                });
            } else {
                return console.log("The user is the creator");
            }
        }
    });

});

exports.noteDeleteListener = functions.firestore.document("Notes/{note_id}").onDelete((snap, context) => {
    const note_docID = context.params.note_id;
    let collaboratorList = snap.data().collaboratorList;

    var userIDs = [];
    const batch = admin.firestore().batch();

    collaboratorList.forEach(element => {
        userIDs.push(element.user_id);
        batch.delete(admin.firestore().collection('Users').doc(element.user_id).collection('NotesDetails').doc(note_docID));
    });

    return batch.commit().then(() => {
        return console.log("Deleted note: " + note_docID + ", succesfully deleted user note details from: " + userIDs);
    });

});

exports.noteUpdateListener = functions.firestore.document("Users/{userID}/Notes/{noteID}").onUpdate((change, context) => {
    const userID = context.params.userID;
    const noteID = context.params.noteID;

    const newValue = change.after.exists ? change.after.data() : null;

    let collaboratorList = newValue.collaboratorList;

    if (collaboratorList.length > 1 && newValue.updated_by_cloud_function !== "true") {

        newValue.updated_by_cloud_function = "true";
        const batch = admin.firestore().batch();

        collaboratorList.forEach((collaborator) => {
            if (collaborator.user_email !== newValue.last_edited_by_user) {
                batch.update(admin.firestore().collection("Users").doc(collaborator.user_id).collection("Notes").doc(noteID), {
                    noteText: newValue.noteText,
                    noteTitle: newValue.noteTitle
                });
                batch.set(admin.firestore().collection("Users").doc(collaborator.user_id).collection("Notes").doc(noteID).collection("Edits").doc(), newValue);
                console.log("collaborator: " + collaborator.user_email);
            }
        });

        return batch.commit().then(() => {
            return console.log("Updated notes for collaborators");
        });

    } else {
        console.log('User is the only collaborator or Note was last updated by cloud_function ');
        return true;
    }
});



// exports.helloWorld = functions.https.onRequest((req, res) => {
//     res.send('Hello from firebase function');
// });
