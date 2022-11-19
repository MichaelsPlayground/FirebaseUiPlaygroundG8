# Firebase UI Playground

based on the samplecode in https://github.com/firebase/FirebaseUI-Android

**Authentication** allows log-in with Email-Password and Google account



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