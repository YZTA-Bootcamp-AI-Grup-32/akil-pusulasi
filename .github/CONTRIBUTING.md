# 🧩 CONTRIBUTING.md

## 📌 Branch İsimlendirme Kuralları

| Tür         | Şablon Örnekleri             | Açıklama                           |
|-------------|------------------------------|------------------------------------|
| Feature     | `feature/login-page`         | Yeni bir özellik eklerken          |
| Bugfix      | `bugfix/fix-null-error`      | Hata düzeltmeleri için             |
| Hotfix      | `hotfix/api-timeout`         | Acil ve küçük düzeltmeler          |  
| Refactor    | `refactor/optimize-loop`     | Yapısal/performans iyileştirmeleri |

- İsimler kısa, açıklayıcı ve küçük harflerle yazılmalıdır.
- Yeni branch açmadan önce en güncel `main` branch'inden ayrılın.

## ✨ Commit Mesajı Kuralları

| Etiket     | Ne Zaman Kullanılır                                                                 |
|------------|-------------------------------------------------------------------------------------|
| `feat`     | Yeni bir özellik eklediğinizde                                                      |
| `fix`      | Bir hatayı düzelttiğinizde                                                          |
| `refactor` | Davranışı değiştirmeden kod yapısını geliştirdiğinizde                              |
| `chore`    | Yapılandırma, betik güncellemesi gibi rutin işler için                              |
| `docs`     | Sadece dokümantasyon güncellemeleri için                                            |
| `test`     | Test eklediğinizde veya testleri güncellediğinizde                                  |
| `style`    | Biçimlendirme, girinti, boşluk gibi kodun çalışmasını etkilemeyen düzenlemeler için |
| `perf`     | Performans iyileştirmeleri yaptığınızda                                             |

---

## 🛠️ Pull Request Süreci

1. **PR'ı erkenden açın (tercihen Draft PR):**
   - Kod tamamen bitmeden gözden geçirilmesi kolaylaşır.
   - Başlık ve açıklamada *ne yaptığınız*, *neden yaptığınız*, varsa *ilgili issue numarası* mutlaka yer alsın.

2. **Açıklayıcı commit mesajları yazın:**
   - Kötü: `fix`
   - İyi: `fix: user form validation bug`

3. **Kod İncelemesi:**
   - PR açıldıktan sonra bir ekip üyesi kodu incelemelidir.
   - Gerekirse yorumlar doğrultusunda değişiklik yapılır.

4. **Merge İşlemi:**
   - PR onaylandıktan sonra merge edilir.
   - Merge işleminden sonra ilgili branch silinir.

---

## 🎯 Notlar

- Kod standardına uymayan PR’lar düzenlenmesi için geri çevrilebilir.
- Bu kurallar gelişebilir, ekip içinde konuşularak güncellenebilir.