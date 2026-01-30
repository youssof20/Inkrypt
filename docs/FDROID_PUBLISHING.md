# Publishing Inkrypt on F-Droid

Step-by-step guide to get Inkrypt into the official [F-Droid](https://f-droid.org) repository. F-Droid **builds your app from source**; you don’t upload an APK. You add metadata so their build servers can clone, build, and publish your app.

**Useful links:**
- [F-Droid FAQ for developers](https://f-droid.org/docs/FAQ_-_App_Developers/)
- [fdroiddata repo](https://gitlab.com/fdroid/fdroiddata/) (metadata for all apps)
- [Inclusion policy](https://f-droid.org/docs/Inclusion_Policy/) (check your app qualifies)
- [Build Metadata Reference](https://f-droid.org/docs/Build_Metadata_Reference/) (YAML format)

---

## 1. Make sure your app qualifies

- **License:** Must be FOSS (e.g. MIT, GPL, Apache 2.0). Inkrypt is MIT.
- **Source:** Public Git repo (e.g. GitHub/GitLab) with tags or clear releases.
- **No proprietary deps:** No Google Play Services, Firebase, closed-source SDKs. Inkrypt is offline-only; you’re fine.
- **Version in build files:** `versionName` and `versionCode` in `build.gradle.kts` (or `build.gradle`), **not** computed at build time (no timestamps). Inkrypt uses fixed values; good.
- **Build from command line:** `./gradlew assembleRelease` (or equivalent) must work. F-Droid will run this.

---

## 2. Prepare your repo

1. **Tag releases.** For each release, e.g. `1.0`:
   ```bash
   git tag v1.0
   git push origin v1.0
   ```
   Use a consistent scheme (e.g. always `v1.0`, `v1.1`).

2. **README / license.** Have a clear README and a `LICENSE` file in the repo root.

3. **No secrets in repo.** No API keys or signing keystores in source. F-Droid signs with their own key.

---

## 3. Fork fdroiddata and add your app

F-Droid’s app list lives in the [fdroiddata](https://gitlab.com/fdroid/fdroiddata) repo. You add a metadata file for your app.

1. **Create a GitLab account** (if you don’t have one): [https://gitlab.com](https://gitlab.com).

2. **Fork fdroiddata:**  
   Go to [https://gitlab.com/fdroid/fdroiddata](https://gitlab.com/fdroid/fdroiddata) → **Fork** (top right). Fork to your own namespace.

3. **Clone your fork:**
   ```bash
   git clone https://gitlab.com/YOUR_USERNAME/fdroiddata.git
   cd fdroiddata
   ```

4. **Create a new metadata file.**  
   Filename = **package ID** of your app, with `.yml` extension.  
   For Inkrypt the package ID is `com.betterappsstudio.inkrypt`, so the file is:
   ```text
   metadata/com.betterappsstudio.inkrypt.yml
   ```

5. **Add the YAML content.** Create `metadata/com.betterappsstudio.inkrypt.yml` with something like this (adjust URLs and descriptions to match your repo):

   ```yaml
   Categories:
     - Productivity
   License: MIT
   WebSite: https://github.com/YOUR_USERNAME/Inkrypt
   SourceCode: https://github.com/YOUR_USERNAME/Inkrypt
   IssueTracker: https://github.com/YOUR_USERNAME/Inkrypt/issues

   Summary: Private encrypted journal. Local-only, no accounts. PIN lock, export/import, templates, search.

   Description: |
     Inkrypt is a local-only, encrypted journal for Android. Your entries are stored on device and protected with a PIN. No account, no cloud, no analytics. Supports templates, search, tags, images, export to Markdown or encrypted ZIP, and a quick-capture widget.
     .
     Encryption: Content and database are encrypted (AES-GCM, SQLCipher). Key is derived from your PIN (PBKDF2). Requires Android 8.0+.

   RepoType: git
   Repo: https://github.com/YOUR_USERNAME/Inkrypt.git

   Build:
     - versionName: 1.0
       versionCode: 1
       commit: v1.0
       gradle:
         - yes
       output: app/build/outputs/apk/release/app-release-unsigned.apk
       srclibs:
         - gradle@7.x
   ```

   **Important:**
   - Replace `YOUR_USERNAME` and `Inkrypt` with your actual GitHub (or GitLab) username and repo name.
   - `commit` must match a **tag** in your repo (e.g. `v1.0`). F-Droid checks out that ref to build.
   - `versionName` and `versionCode` must match what’s in your **app’s** `build.gradle.kts` for that tag.
   - If your release APK path or Gradle version differs, adjust `output` and `srclibs`. See [Build Metadata Reference](https://f-droid.org/docs/Build_Metadata_Reference/).

6. **Commit and push:**
   ```bash
   git add metadata/com.betterappsstudio.inkrypt.yml
   git commit -m "Add Inkrypt - private encrypted journal"
   git push origin main
   ```
   (Use `master` if that’s the default branch of fdroiddata.)

---

## 4. Open a Merge Request (MR)

1. On GitLab, open your fork: `https://gitlab.com/YOUR_USERNAME/fdroiddata`.
2. You should see a prompt to **Create Merge Request** from your branch. Click it.
3. Target: **fdroid/fdroiddata**, branch **master** (or **main**).
4. Fill in the MR title/description, e.g. “Add Inkrypt – private encrypted journal (Android)”.
5. Submit the MR.

F-Droid maintainers will review. They may ask for changes (e.g. fix `output` path, add `srclibs`, or tweak description). Reply in the MR and update the metadata file in your fork as requested.

---

## 5. After the MR is merged

- F-Droid’s **build process** runs on a schedule (e.g. daily). They clone your repo at the tag you specified, run the build, and publish the APK.
- **Monitor:** [F-Droid Monitor](https://monitor.f-droid.org/) – search for your app to see if it’s “Need updating”, “Building”, or “Added”.
- **Updates:** When you release a new version (e.g. 1.1), add a new `Build` entry to the same `metadata/com.betterappsstudio.inkrypt.yml` and open another MR. Example:
   ```yaml
   Build:
     - versionName: 1.1
       versionCode: 2
       commit: v1.1
       gradle:
         - yes
       output: app/build/outputs/apk/release/app-release-unsigned.apk
       srclibs:
         - gradle@7.x
     - versionName: 1.0
       versionCode: 1
       ...
   ```
   They prefer **versionCode** to increase with each release.

---

## 6. Optional: Request for Packaging (if you don’t want to do the MR yourself)

If you’d rather not edit fdroiddata:

1. Open [Requests for Packaging](https://gitlab.com/fdroid/fdroiddata/-/issues?label_name%5B%5D=Request+for+Packaging).
2. **New issue.** Title e.g. “Add Inkrypt – private encrypted journal”.
3. In the description, add:
   - Link to your **source repo**
   - Link to **releases / tags**
   - Short description and license.
4. A volunteer or bot may create the metadata and MR for you. It can take time; doing the MR yourself is usually faster.

---

## 7. Checklist before you submit

- [ ] App builds with `./gradlew assembleRelease` (or your release task).
- [ ] No proprietary libraries (no Play Services, Firebase, etc.).
- [ ] License file in repo (e.g. MIT in `LICENSE`).
- [ ] At least one **tag** in the repo (e.g. `v1.0`) matching the version in `build.gradle.kts`.
- [ ] `metadata/com.betterappsstudio.inkrypt.yml` has correct **Repo**, **commit**, **versionName**, **versionCode**, and **output** path.
- [ ] Summary and Description are clear and match your app (see [METADATA.md](METADATA.md) for text).

---

## 8. If the build fails on F-Droid

- Check [F-Droid Monitor](https://monitor.f-droid.org/) for your app and open the build log.
- Common issues:
  - **Wrong `output` path** – Must match the APK path your Gradle build produces (e.g. `app/build/outputs/apk/release/app-release-unsigned.apk`).
  - **Wrong `commit`** – Must be a tag or commit that exists in your repo.
  - **Missing dependencies** – All deps must be from allowed repos (e.g. Maven Central). No private or Google-only repos unless documented.
  - **JDK / Gradle version** – You can specify `srclibs` (e.g. gradle version) in the metadata; see [Build Metadata Reference](https://f-droid.org/docs/Build_Metadata_Reference/).

Fix the metadata (or your build) and submit a new MR or push a new commit to your MR.

---

**Summary:** Prepare repo + tags → fork fdroiddata → add `metadata/com.betterappsstudio.inkrypt.yml` → push → open MR to fdroid/fdroiddata → wait for review and build. For more detail, see the [F-Droid FAQ for developers](https://f-droid.org/docs/FAQ_-_App_Developers/) and the [fdroiddata](https://gitlab.com/fdroid/fdroiddata) repo.
