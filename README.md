# חנות אפליקציות מותאמת (Aurora Store Fork)

גרסה מותאמת (Fork) של **Aurora Store** (לקוח קוד פתוח ל-Google Play) וספריית התקשורת **`gplayapi`**.

מאגר זה מותאם במיוחד עבור **סביבות עם סינון תכנים (כגון נטפרי)** ועבור **רומים מותאמים/כשרים (כגון DumberOS)**, ומספק חנות אפליקציות מבוקרת, מסוננת והיברידית.

---

## 🌟 תכונות ושינויים מרכזיים בפורק

- 🛡️ **אכיפת רשימה לבנה (Whitelist) מחמירה**:
  רק אפליקציות המאושרות בקובץ הרשימה הלבנה החיצוני מוצגות בחנות, ניתנות לחיפוש או נפתחות באמצעות קישורים ישירים (Deep-links). כל אפליקציה אחרת נחסמת לחלוטין.
- 🔄 **הורדת אפליקציות מותאמות/מתוקנות (Patched APKs)**:
  עבור אפליקציות נבחרות (כגון WhatsApp, Waze, Bit, Spotify ועוד), בקשות ההורדה והעדכון מנותבות ישירות ממאגר השחרורים ב-GitHub ([cfopuser/app-store](https://github.com/cfopuser/app-store)) במקום משרתי Google Play.
- 📦 **תמיכה באפליקציות שאינן ב-Google Play**:
  תמיכה מלאה בהצגת מידע, תמונות, תיאורים בעברית והתקנה של אפליקציות שאינן קיימות כלל בחנות הרשמית (כגון MetroList, Meld, Termux) באמצעות מטא-דאטה מבוסס `app.json`.
- ⏱️ **ניהול עדכונים וגרסאות חכם (Patch in Progress)**:
  מניעת לולאות עדכון כאשר קיימת גרסה חדשה ב-Google Play אך גרסת הפאץ' המותאמת טרם פורסמה. במצב זה מוצג כפתור ייעודי מושבת עם הכיתוב **"פאצ' בהכנה"** כדי למנוע דריסת אפליקציה מותאמת בגרסה רגילה.
- ⚡ **מטמון עמיד וביצועים מהירים**:
  שמירת קטגוריות ורשימות בזיכרון למעבר מיידי בין מסכים ללא טעינות חוזרות והבהובים, לצד טיפול חסין בעבודה ללא חיבור רשת (Offline).

---

## 🏗️ מבנה המערכת והאקוסיסטם

המערכת מורכבת מ-3 רכיבים עיקריים הפועלים בסנכרון:

1. **אפליקציית הלקוח (מאגר זה)**: מבוססת Kotlin ו-Jetpack Compose, כוללת את ספריית `gplayapi` עם המודולים `WhitelistManager` ו-`PatchedAppManager`.
2. **מנהל הרשימה הלבנה (Whitelist Dashboard)**: מנהל את קטלוג האפליקציות המאושרות והחלוקה לקטגוריות (`categorized-whitelist.json`).
3. **מפעל הפאצ'ים האוטומטי (`cfopuser/app-store`)**: מערכת CI/CD המייצרת גרסאות מותאמות ומפרסמת שחרורים וקובץ `releases.json`.

---

## 🚀 תכונות כלליות של Aurora Store

- **קוד פתוח (FOSS)**: ברישיון GPLv3.
- **עיצוב Material 3**: ממשק מודרני, נקי ואינטואיטיבי.
- **התחברות לחשבון**: תמיכה בהתחברות אנונימית או בחשבון גוגל אישי באמצעות microG.
- **מנהל הורדות והתקנות**: הורדה מאובטחת, אימות קבצים והתקנה שקטה/ידנית.
- **פרטיות**: בדיקת מעקבים באמצעות Exodus Privacy.

---

## 📋 הרשאות עיקריות

- `INTERNET` / `ACCESS_NETWORK_STATE`: גישה לרשת ובדיקת חיבור פעיל.
- `REQUEST_INSTALL_PACKAGES` / `REQUEST_DELETE_PACKAGES`: התקנה, עדכון והסרת אפליקציות.
- `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_DATA_SYNC`: סנכרון והורדה של קבצים ברקע ללא הפרעות.
- `UPDATE_PACKAGES_WITHOUT_USER_ACTION` / `ENFORCE_UPDATE_OWNERSHIP`: עדכון אוטומטי ושקט (במכשירים נתמכים).

---
### 🎛️ דשבורד הניהול וה-Backend
הרשימה הלבנה, קטלוג ה-AI ובקשות האפליקציות של המשתמשים מנוהלים בריפו ייעודי:
* **מאגר הדשבורד:** [chanuta159-design/aurora-whitelist](https://github.com/chanuta159-design/aurora-whitelist)
* **דשבורד חי ב-Vercel:** [aurora-whitelist-chi.vercel.app](https://aurora-whitelist-chi.vercel.app)

**אינטגרציה מול האפליקציה:**
* `WhitelistManager.kt` מושך את הרשימה המאושרת מתוך `categorized-whitelist.json`.
* `AppRequestScreen.kt` שולח בקשות משתמש ישירות ל-API בכתובת `/api/request-app`.
## 📄 רישיון וקרדיטים

פרויקט זה מבוסס על [Aurora Store](https://gitlab.com/AuroraOSS/AuroraStore) ומשוחרר תחת רישיון **GNU General Public License v3.0 (GPLv3)**.

תודות למפתחים ולפרויקטים המקוריים:
- [Aurora Store](https://gitlab.com/AuroraOSS/AuroraStore)
- [YalpStore](https://github.com/yeriomin/YalpStore)
- [AppCrawler](https://github.com/Akdeniz/google-play-crawler)
- [Raccoon](https://github.com/onyxbits/raccoon4)
- [SAI](https://github.com/Aefyr/SAI)
