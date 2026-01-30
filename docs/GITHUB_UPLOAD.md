# Upload Inkrypt to GitHub

**What to upload:** The **full open source project** (all source code). That’s what belongs in your portfolio and what people expect for “open source.”

**Optional:** After the repo is up, create a **Release** and attach the **APK** so visitors can install without building. One repo = source + optional release APK.

---

## Steps

### 1. Create a new repo on GitHub

1. Go to [github.com](https://github.com) and sign in.
2. Click **+** (top right) → **New repository**.
3. **Repository name:** e.g. `Inkrypt` (or `inkrypt`).
4. **Description:** e.g. `Private encrypted journal for Android. Local-only.`
5. **Public.**
6. **Do not** check “Add a README” (you already have one).
7. Click **Create repository**.

---

### 2. Upload from your machine

In a terminal, in your project folder (where `build.gradle.kts` is):

```bash
# If this folder is not a git repo yet:
git init

# Add everything (respects .gitignore)
git add .

# First commit
git commit -m "Initial commit: Inkrypt - private encrypted journal"

# Rename branch to main if needed
git branch -M main

# Add your GitHub repo as remote (replace YOUR_USERNAME and REPO_NAME with yours)
git remote add origin https://github.com/YOUR_USERNAME/REPO_NAME.git

# Push
git push -u origin main
```

If you already use Git and have a different remote, use your usual flow and push to the new repo.

---

### 3. Add the topic for your portfolio

On GitHub:

1. Open your repo.
2. Click the **gear** next to “About” (right side).
3. In **Topics**, add: `mobile-app` (and anything else you want, e.g. `android`, `kotlin`, `journal`).
4. Save.

---

### 4. (Optional) Add a release with the APK

1. Build a release APK (unsigned is fine for a portfolio):
   ```bash
   ./gradlew assembleRelease
   ```
   APK path: `app/build/outputs/apk/release/app-release-unsigned.apk`

2. On GitHub: **Releases** → **Create a new release**.
3. **Tag:** e.g. `v1.0` (create the tag).
4. **Title:** e.g. `v1.0`.
5. In the description you can paste the README or a short summary.
6. **Attach the APK:** drag `app-release-unsigned.apk` into “Attach binaries.”
7. Publish.

Visitors can then clone the repo (source) and/or download the APK from Releases.

---

## Checklist before first push

- [ ] No passwords, API keys, or signing keystores in the repo (`.gitignore` already ignores `*.jks`, `*.keystore`, `local.properties`).
- [ ] README and LICENSE are in the repo root.
- [ ] Project builds: `./gradlew assembleDebug`.

That’s it. Full project on GitHub + topic `mobile-app` + optional release APK.
