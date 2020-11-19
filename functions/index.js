
/* eslint-disable promise/no-nesting */
'use-strict'

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
                const payload = {
                    data: {
                        noteID: note_docID,
                        title: "New note",
                        body: "You have been added as a collaborator to a new note!",
                        icon: "default",
                        click_action: "com.rokudoz.onotes.MainActivity"
                    }
                };
                return admin.messaging().sendToDevice(user_tokenID, payload).then(result => {
                    return console.log("Notification sent");
                });
            } else {
                return console.log("The user is the creator");
            }
        }
    });

});