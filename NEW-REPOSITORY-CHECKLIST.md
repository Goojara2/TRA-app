# New repository checklist

Use this project as a fresh baseline.

- [ ] Create a new empty GitHub repository.
- [ ] Upload only the v1.0.0 project files.
- [ ] Do not copy any v2.x/v3.x workflow or Android source into this repository.
- [ ] Confirm `.github/workflows/` contains only:
  - `android-build.yml`
  - `android-signed-release.yml`
- [ ] Run the normal APK/AAB workflow.
- [ ] Download and install the debug APK.
- [ ] Uninstall any previous experimental TRA app before installing this v1.0.0 build.
- [ ] Test Home, Products, Events, Gallery and Book.
- [ ] Test login and My Account.
- [ ] Test profile image/file upload.
- [ ] Confirm the website footer does not appear inside the app.
- [ ] Confirm the original TRA logo appears at startup.
- [ ] Only after the debug app is stable, configure Play Store signing.
