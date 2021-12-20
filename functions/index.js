const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase)
admin.firestore().settings({ timestampsInSnapshots: true })


exports.notesCollaboratorsListener = functions.firestore.document("Users/{userID}/NotesDetails/{documentID}").onCreate((snap, context) => {
    const user_id = context.params.userID;
    const note_docID = context.params.documentID;

    const user_data = admin.firestore().collection("Users").doc(user_id).get();
    const note_data = admin.firestore().collection("Notes").doc(note_docID).get();

    return Promise.all([user_data, note_data]).then(result => {
        const user_email = result[0].data().email;
        const note_creator_email = result[1].data().creator_user_email;

        const user_tokenID = result[0].data().user_device_token;

        if (user_tokenID === null) {
            return console.log("User token is null");
        } else {
            if (user_email !== note_creator_email) {
                const options = {
                    priority: 'high'
                }
                const payload = {
                    data: {
                        noteID: note_docID,
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

    const oldValue = change.before.exists ? change.before.data() : null;
    const newValue = change.after.exists ? change.after.data() : null;
    const newDate = change.after.exists ? change.after.data().date : null;
    const oldDate = change.before.exists ? change.before.data().date : null;


    let collaboratorList = newValue.collaboratorList;

    if (newValue.last_edited_by_user !== "admin") {
        console.log('Last updated by other user ');
        return admin.firestore().collection("Users").doc(userID).collection("Notes").doc(noteID).update({
            last_edited_by_user: "admin"
        })
    } else {
        return console.log('Last updated by admin ');
    }

});

exports.helloWorld = functions.https.onRequest((req, res) => {
    res.send('Hello from firebase function');
});
