# Ses Tanıma İle Eşya Bulma Sistemi

İnsan uygarlığının gelişmesiyle birlikte zamandan tasarruf etmek amacıyla çok  sayıda araç gereç icat edilmiştir. Bu araç gereçler boşalan vaktin farklı şekilde  kullanılmasını sağladığı gibi yeni vakit kayıplarına da neden olmuştur. Kişisel  bilgisayarlar sayesinde birçok eşya tarihe gömülmüş; çekmecelerden mektuplar,  fotoğraf albümleri, çeşitli medya oynatıcılar gibi çeşitli eşyalar kalkmıştır. Ancak  bunların yerini taşınabilir diskler, kablosuz fareler, klavyeler, dönüştürücüler, çeşitli  kablolar, şarj aletleri, monitörler, sanal gerçeklik gözlükleri gibi yeni eşyalar almıştır.  Dolayısıyla eşya boyutu azalmış, ancak eşya çeşidi artmıştır. “Bilgisayar eşyaları,  telefon eşyaları, kamera eşyaları” gibi sınıflandırmalar her iki sınıfa da girebilen  eşyalar nedeniyle yetersiz kalmış, bu da birçok eşyanın arama esnasında vakit  kaybına yol açmasıyla sonuçlanmıştır. 

### 1.1. Problemin Tanımı

Eşya çeşidinin artmasıyla birlikte eşyaların aranma süreci hafızayla ilgili sorunları  olan insanlarda ve görme engellilerde daha belirgin olmakla birlikte bir problem  hâline gelmiştir. Bu işlemin yükü insan beyninden alınıp dijital asistanlara 
devredilmelidir. 

### 1.2. Geliştirilen Çözüm 

Kullanıcı tarafından eşyayı tanımlayıcı bilgi mobil uygulamaya sesli veya yazılı  olarak bildirilir ve eşyanın bulunduğu bölgeden sinyal alınır. 

### 1.2.1.Sistemin Kurulumu 

Evde veya ofiste eşyaların bulunduğu bölgeler gruplanarak bu bölgeler sistem  üzerinde etiketlenir. Kurulum yapılırken ayrılan bölge sayısınca alınacak cihazlar tespit edilen bölgelere yerleştirilir. Bu bölgeler, kullanıcının tercihine göre oda veya  odanın bölümleri, hatta raflar olarak seçilebilir. 

### 1.2.2.Eşyaların Kaydedilmesi 

Kullanıcı, bölgenin ismiyle birlikte eşyanın ismini ve aynı isimde olabilecek başka  eşyalara karşılık eşyayı tanımlayacak anahtar kelimeleri mobil uygulamada sesli  komut vererek veya kaydetme ekranı üzerinden eşyayı kaydedebilir. 

### 1.2.3.Eşyanın Konumunun Tespit Edilmesi 

Ulaşılmak istenen eşya doğrudan sistemde kaydedilen ismi telaffuz edilerek yahut  eşyayı tanımlayacak yeterince anahtar kelime söylenerek mobil uygulama üzerinden  sesli bir şekilde aranır. Eğer eşyayı tanımlayacak yeterli veri alındıysa ve veri  tabanında eşsiz bir eşya bulunduysa, eşyanın kayıtlı olduğu bölgeden sesle (ve  opsiyonel olarak ışıkla) uyarı yapılır ve kişi o bölgeye yönlendirilir. Aynı şekilde bu  işlem doğrudan kullanıcı ara yüzü ile de yapılabilir.

## Adım Adım Çalışma Şekli
- Evinizdeki bölgeleri isimlendirin.
- Her bölgeye bir NodeMCU kartı istediğiniz donanımı (buzzer, LED vs.) bağlayarak yerleştirin
- NodeMCU kartları aşağıdaki linkte belirtildiği gibi programlayın <b>  [[1]](#NodeMCU-Kodları) </b>
- Kartı çalıştırdıktan sonra telefonunuzdan Wi-Fi ağlarını listeleyin ve KurulumAP ismindeki ağa bağlanın. Yönlendiricinizin SSID ve parolasını girerek kartı ağa bağlayın. Bu adımı her kart için tekrarlayın.
- Android uygulamasını cihazınıza yükleyin ve evinizin ağına bağlanın
- Uygulama üzerinden her bölgedeki cihazı kaydedin  <b>  [[2]](#Android-Uygulamasında-Kurulum) </b>
- Eşyalarınızı dilerseniz ortadaki :heavy_plus_sign: (Eşya Ekle) butonu ile,<br>
dilerseniz sağ alttaki :studio_microphone: (Ses Tanıma) butonu ile <b>[Bölge Adı] ekle [Eşya Adı]</b> komut dizisini kullanarak ekleyin. Bu aşamadan sonra sistem çalışmaya hazırdır.
- Eşya aramak için :studio_microphone: (Ses Tanıma) simgesine dokunun ve <b>[Eşya Adı] nerede</b> komutunu kullanın. Eğer söylediğiniz eşya isminin kullanıldığı başka bir eşya daha yok ise seçtiğiniz bölgedeki karttan bağladığınız donanıma göre sinyal (ışık-ses) gelecektir.<br>

## Android Uygulamasında Kurulum
<img src="resimler/Screenshot_20200601-083357.png" height="400"> <img src="resimler/Screenshot_20200601-083405.png" height="400"> <img src="resimler/Screenshot_20200601-083409.png" height="400"> <img src="resimler/Screenshot_20200601-083414.png" height="400"> <img src="resimler/Screenshot_20200601-083421.png" height="400"> <img src="resimler/Screenshot_20200601-083435.png" height="400"> <img src="resimler/Screenshot_20200601-083502.png" height="400"> <img src="resimler/Screenshot_20200601-083526.png" height="400"> <img src="resimler/Screenshot_20200601-083605.png" height="400"> <img src="resimler/Screenshot_20200601-083732.png" height="400"> <img src="resimler/Screenshot_20200601-083755.png" height="400">

## Eşya Kaydetme
Eşyalarınızı dilerseniz ortadaki :heavy_plus_sign: (Eşya Ekle) butonu ile,<br>
dilerseniz sağ alttaki :studio_microphone: (Ses Tanıma) butonu ile <b>[Bölge Adı] ekle [Eşya Adı]</b> komut dizisini kullanarak ekleyin. Bu aşamadan sonra sistem çalışmaya hazırdır.<br>

<img src="resimler/Screenshot_20200601-083932.png" height="400"> <img src="resimler/Screenshot_20200601-083936.png" height="400"> <img src="resimler/Screenshot_20200601-084035.png" height="400"> <img src="resimler/Screenshot_20200601-084038.png" height="400"> <img src="resimler/Screenshot_20200601-084046.png" height="400"> <img src="resimler/Screenshot_20200601-090204.png" height="400">

## Eşya Arama
:studio_microphone: (Ses Tanıma) simgesine dokunun ve <b>[Eşya Adı] nerede</b> komutunu kullanın. Eğer söylediğiniz eşya isminin kullanıldığı başka bir eşya daha yok ise seçtiğiniz bölgedeki karttan bağladığınız donanıma göre sinyal (ışık-ses) gelecektir.<br>

<img src="resimler/Screenshot_20200601-084141.png" height="400"><img src="resimler/Screenshot_20200601-084153.png" height="400">

Eğer aynı ismin kullanıldığı başka eşyalar da var ise bunlar telefonunuzda listelenecektir.

<img src="resimler/Screenshot_20200601-085242.png" height="400"><img src="resimler/Screenshot_20200601-085245.png" height="400">



## Sistem Mimarisi
<img src="resimler/SistemMimarisi.png" height="400"> 


## Use-Case
<img src="resimler/UseCase.png" height="400">



## NodeMCU Kodları
https://github.com/melihcelenk/EsyaBulmaSistemi_NodeMCU

