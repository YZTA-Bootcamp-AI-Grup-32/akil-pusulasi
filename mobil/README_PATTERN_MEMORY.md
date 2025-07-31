# Pattern Memory Oyunu - Akıl Pusulası

## Proje Açıklaması

Bu proje, yaşlılar ve hafif bilişsel bozukluk yaşayan bireyler için tasarlanmış bir bilişsel sağlık asistanı olan Akıl Pusulası uygulamasının bir parçasıdır. Pattern Memory oyunu, kullanıcının görsel hafıza ve dikkat becerilerini ölçen basit bir hafıza oyunudur.

## Oyun Özellikleri

### Temel Özellikler
- **Görsel Hafıza Testi**: Belirli bir deseni hatırlama ve tekrar oluşturma
- **Kapsamlı Zorluk Sıralaması**: 25 seviyeli kademeli ve adaptif zorluk artışı
- **Yaşlı Dostu Arayüz**: Büyük butonlar ve okunaklı metinler
- **Anlık Geri Bildirim**: Doğru/yanlış cevap gösterimi ve yeşil vurgu
- **Dinamik Süre Sistemi**: Seviyeye göre adaptif yanma süreleri

### Oyun Mekaniği
1. **Desen Gösterimi**: Sistem rastgele hücreleri tane tane yanıp sönen desen olarak gösterir
2. **Hafıza Süreci**: Kullanıcı deseni hatırlamaya çalışır
3. **Tekrar Oluşturma**: Kullanıcı gördüğü deseni aynı sırayla hücrelere tıklayarak tekrar oluşturur
4. **Sonuç Değerlendirme**: Sistem kullanıcının seçimini kontrol eder

### Kapsamlı Zorluk Sıralaması

#### Aşama 1: Temelleri Sağlamlaştırma (3x3 Izgara)
- **Seviye 1-3**: 2 kutucuk yanar
- **Seviye 4-6**: 3 kutucuk yanar

#### Aşama 2: Yeni Boyutlara Geçiş (4x4 Izgara)
- **Seviye 7-9**: 4x4 ızgarada 3 kutucuk yanar
- **Seviye 10-12**: 4x4 ızgarada 4 kutucuk yanar
- **Seviye 13-15**: 4x4 ızgarada 5 kutucuk yanar

#### Aşama 3: İleri Seviye (5x5 Izgara)
- **Seviye 16-18**: 5x5 ızgarada 5 kutucuk yanar
- **Seviye 19-21**: 5x5 ızgarada 6 kutucuk yanar
- **Seviye 22-25**: 5x5 ızgarada 7 kutucuk yanar

### Dinamik Süre Sistemi
- **Başlangıç**: 1200ms yanma, 500ms bekleme
- **Kademeli Azalma**: Her seviyede 35ms yanma, 18ms bekleme azalma (daha belirgin)
- **Minimum Limitler**: 600ms yanma, 250ms bekleme
- **Sırayla Yanıp Sönme**: Her kutucuk kendi döngüsünü tamamladıktan sonra bir sonraki yanar
- **Toplam Döngü Süresi**: Yanma + Bekleme süresi
- **Kesintisiz Akış**: "Yan", "Sön", "Bekle", "Yan", "Sön", "Bekle" şeklinde
- **Yaşlı Dostu Hız**: Süreler asla ani hızlanma yapmaz, kontrollü kademeli azalma

### Oyun Sabitleri
```kotlin
INITIAL_ILLUMINATE_DELAY_MS: 1200L // 1.2 saniye yanık kalma
MIN_ILLUMINATE_DELAY_MS: 600L // 0.6 saniye minimum yanma
INITIAL_PAUSE_BETWEEN_CELLS_MS: 500L // 0.5 saniye bekleme
MIN_PAUSE_BETWEEN_CELLS_MS: 250L // 0.25 saniye minimum bekleme
DELAY_REDUCTION_PER_LEVEL_MS: 35L // Her seviyede 35ms azalma (daha belirgin)
PAUSE_REDUCTION_PER_LEVEL_MS: 18L // Her seviyede 18ms azalma (daha belirgin)
MAX_GAME_LEVEL: 25 // Maksimum oyun seviyesi
```

## Teknik Detaylar

### Kullanılan Teknolojiler
- **Dil**: Kotlin
- **UI Framework**: Android Views (XML Layouts)
- **Minimum SDK**: Android 21 (Lollipop)
- **Target SDK**: Android 35

### Dosya Yapısı
```
app/src/main/
├── java/com/example/akilpusulasi/
│   ├── PatternMemoryActivity.kt    # Ana oyun aktivitesi
│   └── MainActivity.kt             # Ana menü
├── res/
│   ├── layout/
│   │   └── activity_pattern_memory.xml  # Oyun layout'u
│   ├── values/
│   │   ├── colors.xml              # Renk tanımları
│   │   └── strings.xml             # Metin tanımları
│   └── AndroidManifest.xml         # Aktivite tanımları
```

### Oyun Akışı
1. **Başlangıç**: Kullanıcı "Oyuna Başla" butonuna tıklar
2. **Desen Oluşturma**: Sistem rastgele hücreleri seçer ve tane tane yanıp söner
3. **Kullanıcı Girişi**: Kullanıcı hatırladığı hücrelere doğru sırayla tıklar
4. **Değerlendirme**: Sistem doğruluğu ve sırayı kontrol eder
5. **Sonuç**: Doğru ise seviye artar, yanlış ise oyun biter

### Renk Sistemi
- **Varsayılan Hücre**: Gri (#E0E0E0)
- **Yanan Hücre**: Mavi (#2196F3)
- **Seçilen Hücre**: Mor (#9C27B0)
- **Doğru Cevap**: Yeşil (#4CAF50)
- **Yanlış Cevap**: Kırmızı (#F44336)

## Kurulum ve Çalıştırma

### Gereksinimler
- Android Studio Arctic Fox veya üzeri
- Java 11 veya üzeri
- Android SDK 21+

### Kurulum Adımları
1. Projeyi Android Studio'da açın
2. Gradle sync işlemini bekleyin
3. Bir Android emülatörü veya fiziksel cihaz bağlayın
4. "Run" butonuna tıklayın

### Test Senaryoları
- [x] Oyun başlatma
- [x] Desen gösterimi
- [x] Kullanıcı girişi
- [x] Doğru cevap senaryosu
- [x] Yanlış cevap senaryosu
- [x] Seviye geçişleri
- [x] Zorluk artışı
- [x] Yeşil vurgu sistemi
- [x] Dinamik süre sistemi
- [x] Kapsamlı zorluk sıralaması
- [x] Sıralama kontrolü
- [x] Maksimum seviye kontrolü
- [x] Sırayla yanıp sönme akışı
- [x] Yaşlı dostu dinamik süre ayarlaması
- [x] Kontrollü hızlanma temposu
- [x] Izgara yeniden oluşturma düzeltmesi

## Gelecek Geliştirmeler

### Planlanan Özellikler
- [ ] Ses efektleri
- [ ] Animasyonlar
- [ ] İstatistik takibi
- [ ] Farklı oyun modları
- [ ] Sosyal özellikler
- [ ] Backend entegrasyonu

### İyileştirme Önerileri
- [x] Daha fazla görsel geri bildirim
- [x] Özelleştirilebilir zorluk seviyeleri
- [ ] Erişilebilirlik özellikleri
- [ ] Çoklu dil desteği
- [ ] Offline çalışma modu

