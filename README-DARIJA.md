# Z8 Project IA

تطبيق Android بسيط للمحادثة مع API متوافق مع OpenAI Chat Completions عبر Respan.

## شنو كاين فالمشروع؟
- واجهة Chat عربية/دارجة.
- خانة API Base URL و Model و API Key من داخل التطبيق.
- إرسال الرسائل إلى `POST /chat/completions`.
- GitHub Actions كيبني APK Debug تلقائياً عند Push أو يدوياً.

## الإعداد الأول
1. ثبت التطبيق.
2. ضغط على ⚙.
3. Base URL الافتراضي: `https://gateway.respan.ai/v1`
4. دخل اسم الموديل المتوفر فحساب Respan ديالك. `gpt-4o-mini` غير قيمة افتراضية وقد تحتاج تبدلها.
5. دخل Respan API Key ديالك واضغط حفظ.

**الأمان:** ما تحطش API Key داخل GitHub ولا فالكود. المفتاح كيتخزن محلياً فـ SharedPreferences ديال التطبيق. هاد النسخة مناسبة للتجربة الشخصية؛ تطبيق عمومي للناس خاصو Backend وسيط باش ما يتكشفش المفتاح.

## بناء APK عبر GitHub
الـ workflow موجود فـ `.github/workflows/android.yml`.
من بعد رفع المشروع إلى الفرع `main`:
- دخل Actions فـ GitHub.
- اختار Build Z8 Project IA APK.
- ضغط Run workflow إذا ما بداش تلقائياً.
- من بعد ما يكمل بنجاح، حل Artifacts وحمّل `Z8-Project-IA-debug-apk`.
- فك ZIP ديال Artifact وغادي تلقى `app-debug.apk`.

## المتطلبات التقنية
- Android min SDK 23.
- JDK 17.
- Android Gradle Plugin 8.13.2 / Gradle 8.13.
