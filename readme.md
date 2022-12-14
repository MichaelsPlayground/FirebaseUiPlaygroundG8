# Firebase UI Playground

based on the samplecode in https://github.com/firebase/FirebaseUI-Android

**Authentication** allows log-in with Email-Password and Google account

https://firebase.google.com/docs/auth

https://firebaseopensource.com/projects/firebase/firebaseui-android/auth/readme

**Realtime database** 

https://firebase.google.com/docs/database

https://firebaseopensource.com/projects/firebase/firebaseui-android/database/readme/

**Cloud Firestore**

https://firebase.google.com/docs/firestore

https://firebaseopensource.com/projects/firebase/firebaseui-android/firestore/readme

**Storage**

https://firebase.google.com/docs/storage

https://firebaseopensource.com/projects/firebase/firebaseui-android/storage/readme

https://www.geeksforgeeks.org/how-to-populate-recyclerview-with-firebase-data-using-firebaseui-in-android-studio/

Realtime Database rules (**Note: these rules are insecure**):
```plaintext
{
  "rules": {
    ".read": "auth.uid != null",
    ".write": "auth.uid != null"
  }
}
```

Storage tutorial: https://firebasetutorials.com/use-firebase-storage/

Source code: https://github.com/Waqas334/LearningCloudStorage 

A good comprehensive article about FirebaseUi (AuthUi, RT DatabaseUi, FirestoreUi and StorageUi): 
https://www.ericthecoder.com/2018/05/05/a-comprehensive-guide-to-firebaseui-the-shortcut-to-authentication-firestore-and-storage/

The demo app: https://github.com/ericdecanini/FirebaseUIDemo

Storage rules:
```plaintext
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if true;
    }
  }
}
```

Storage rules - only authenticated user have access:
```plaintext
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```
