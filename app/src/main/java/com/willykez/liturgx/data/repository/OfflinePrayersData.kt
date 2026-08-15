package com.willykez.liturgx.data.repository

import com.willykez.liturgx.model.Prayer
import com.willykez.liturgx.model.PrayerCategory

/**
 * Offline Swahili Catholic prayer library.
 *
 * Notes:
 * - The collection uses only PrayerCategory values visible in the supplied model:
 *   HOURS, ROSARY, DIVINE_MERCY and TRADITIONAL.
 * - Novenas are represented as complete 9-day guides where the fixed/common
 *   prayers are included and the daily intention/meditation is clearly separated.
 * - Prayer wording can differ between Tanzanian dioceses, prayer books and editions.
 *   For liturgical publication, compare the final wording with the approved
 *   Swahili edition used by your diocese.
 */
object OfflinePrayersData {

    val prayersList = listOf(

        // ---------------------------------------------------------------------
        // BASIC CATHOLIC PRAYERS
        // ---------------------------------------------------------------------

        Prayer(
            id = "sign_of_the_cross",
            title = "Ishara ya Msalaba",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Sala ya kuanza na kumaliza sala",
            text = "Kwa Jina la Baba, na la Mwana, na la Roho Mtakatifu. Amina."
        ),

        Prayer(
            id = "apostles_creed",
            title = "Nasadiki — Kanuni ya Imani",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Kanuni ya Imani ya Mitume",
            text = """
Nasadiki kwa Mungu, Baba Mwenyezi, Muumba wa mbingu na dunia.

Na kwa Yesu Kristo, Mwanawe wa pekee, Bwana wetu;
aliyetungwa kwa Roho Mtakatifu, akazaliwa na Bikira Maria;
akateswa kwa mamlaka ya Ponsyo Pilato, akasulubiwa, akafa, akazikwa;
akashukia kuzimu; siku ya tatu akafufuka katika wafu;
akapaa mbinguni; amekaa kuume kwa Mungu Baba Mwenyezi;
toka huko atakuja kuwahukumu wazima na wafu.

Nasadiki kwa Roho Mtakatifu;
Kanisa takatifu Katoliki;
ushirika wa watakatifu;
maondoleo ya dhambi;
ufufuko wa mwili;
na uzima wa milele. Amina.
            """.trimIndent()
        ),

        Prayer(
            id = "our_father_sw",
            title = "Baba Yetu",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Sala ya Bwana",
            text = """
Baba yetu uliye mbinguni,
jina lako litukuzwe;
ufalme wako ufike;
utakalo lifanyike duniani kama mbinguni.

Utupe leo mkate wetu wa kila siku;
utusamehe makosa yetu,
kama tunavyowasamehe na sisi waliotukosea;
usitutie katika kishawishi,
lakini utuopoe maovuni. Amina.
            """.trimIndent(),
            latinText = """
Pater noster, qui es in caelis,
sanctificetur nomen tuum;
adveniat regnum tuum;
fiat voluntas tua, sicut in caelo et in terra.

Panem nostrum quotidianum da nobis hodie,
et dimitte nobis debita nostra,
sicut et nos dimittimus debitoribus nostris;
et ne nos inducas in tentationem,
sed libera nos a malo. Amen.
            """.trimIndent()
        ),

        Prayer(
            id = "hail_mary_sw",
            title = "Salamu Maria",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Maamkio ya Malaika",
            text = """
Salamu Maria, umejaa neema, Bwana yu nawe.
Umebarikiwa kuliko wanawake wote,
na Yesu, mzao wa tumbo lako, amebarikiwa.

Maria Mtakatifu, Mama wa Mungu,
utuombee sisi wakosefu,
sasa na saa ya kufa kwetu. Amina.
            """.trimIndent(),
            latinText = """
Ave Maria, gratia plena, Dominus tecum.
Benedicta tu in mulieribus,
et benedictus fructus ventris tui, Iesus.

Sancta Maria, Mater Dei,
ora pro nobis peccatoribus,
nunc et in hora mortis nostrae. Amen.
            """.trimIndent()
        ),

        Prayer(
            id = "glory_be_sw",
            title = "Atukuzwe Baba",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Sifa kwa Utatu Mtakatifu",
            text = """
Atukuzwe Baba, na Mwana, na Roho Mtakatifu,
kama mwanzo, na sasa, na siku zote,
na milele na milele. Amina.
            """.trimIndent(),
            latinText = "Gloria Patri, et Filio, et Spiritui Sancto. Sicut erat in principio, et nunc, et semper, et in saecula saeculorum. Amen."
        ),

        Prayer(
            id = "fatima_prayer",
            title = "Ee Yesu Wangu — Sala ya Fatima",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Sala inayotumiwa baada ya kila fumbo la Rozari",
            text = """
Ee Yesu wangu, utusamehe dhambi zetu,
utuokoe na moto wa milele,
uongoze roho zote mbinguni,
hasa za wale wanaohitaji huruma yako zaidi. Amina.
            """.trimIndent()
        ),

        Prayer(
            id = "act_of_faith",
            title = "Tendo la Imani",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Tendo la kimaadili la Imani",
            text = """
Ee Mungu wangu, nasadiki kwa uthabiti yote ambayo Kanisa Katoliki
linatufundisha kwa sababu wewe ndiye ukweli usioweza kudanganya
na umelifunulia Kanisa lako ukweli huo.
Katika imani hii nataka kuishi na kufa. Amina.
            """.trimIndent()
        ),

        Prayer(
            id = "act_of_hope",
            title = "Tendo la Matumaini",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Tendo la kimaadili la Matumaini",
            text = """
Ee Mungu wangu, kwa kuwa wewe ni mwaminifu na mwenye uwezo wote,
natumaini kwa uthabiti kwamba kwa wema wako nitapata uzima wa milele
na neema zote ninazohitaji ili kuufikia.
Kwa msaada wa neema yako nataka kuishi kwa uaminifu na kufika kwako. Amina.
            """.trimIndent()
        ),

        Prayer(
            id = "act_of_charity",
            title = "Tendo la Mapendo",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Tendo la kumpenda Mungu na jirani",
            text = """
Ee Mungu wangu, nakupenda kwa moyo wangu wote,
kwa sababu wewe ni mwema na unastahili upendo wote.
Kwa ajili yako ninawapenda pia jirani zangu,
na ninataka kutenda mapenzi yako katika maisha yangu yote. Amina.
            """.trimIndent()
        ),

        Prayer(
            id = "act_of_contrition_sw",
            title = "Tendo la Toba",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Sala ya kujutia dhambi",
            text = """
Ee Mungu wangu, najuta kwa moyo wangu wote kwa kuwa nimekukosea.
Nachukia dhambi zangu zote kwa sababu zinakutukana wewe,
Mungu wangu uliye mwema na unayestahili kupendwa kuliko yote.

Kwa msaada wa neema yako,
ninaazimia kutotenda dhambi tena
na kuepuka nafasi zinazonipeleka kwenye dhambi.
Amina.
            """.trimIndent()
        ),

        Prayer(
            id = "memorare",
            title = "Kumbuka, Ee Mwingi wa Huruma",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Sala ya kumwomba Bikira Maria",
            text = """
Kumbuka, Ee Bikira Maria mwenye huruma,
haijawahi kusikika kwamba mtu aliyekimbilia ulinzi wako,
aliomba msaada wako na kutafuta maombezi yako,
akaachwa bila msaada.

Nikiwa nimejaa tumaini hili,
nakimbilia kwako, Ee Bikira wa mabikira, Mama yangu.
Naja kwako, na mbele yako nasimama mwenye dhambi na mwenye huzuni.
Ee Mama wa Neno aliyefanyika mwili,
usipuuze maombi yangu,
bali kwa wema wako yasikie na kuyajibu. Amina.
            """.trimIndent()
        ),

        Prayer(
            id = "hail_holy_queen",
            title = "Salve Regina — Salamu Ee Malkia",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Sala ya kumalizia Rozari",
            text = """
Salamu, Ee Malkia, Mama wa huruma,
uzima wetu, utamu wetu na tumaini letu, salamu!

Tunakulilia sisi wana wa Eva walio uhamishoni;
tunaugua na kulia katika bonde hili la machozi.

Ee mwombezi wetu,
utuangalie kwa macho yako yenye huruma,
na baada ya uhamisho huu utuonyeshe Yesu,
mzao mbarikiwa wa tumbo lako.

Ee mpole, Ee mwenye upendo,
Ee Maria Bikira mwaminifu.

Utuombee, Ee Mzazi Mtakatifu wa Mungu,
ili tustahili ahadi za Kristo.

Tuombe:
Ee Mungu, Mwanao wa pekee kwa maisha yake,
kifo chake na ufufuko wake,
ametupatia zawadi ya uzima wa milele;
tujalie kwamba tunapoyatafakari mafumbo haya
katika Rozari Takatifu ya Bikira Maria,
tufuate yale yanayofundisha
na tupate yale yanayoahidi.
Kwa njia ya Kristo Bwana wetu. Amina.
            """.trimIndent()
        ),

        // ---------------------------------------------------------------------
        // ANGELUS / REGINA CAELI
        // ---------------------------------------------------------------------

        Prayer(
            id = "angelus_sw",
            title = "Angelus — Malaika wa Bwana",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Sala ya asubuhi, adhuhuri na jioni",
            text = """
K. Malaika wa Bwana alimletea Maria ujumbe,
W. Naye akachukua mimba kwa uwezo wa Roho Mtakatifu.
Salamu Maria...

K. Tazama, mimi ni mjakazi wa Bwana,
W. Nitendewe kama ulivyosema.
Salamu Maria...

K. Naye Neno alifanyika mwili,
W. Akakaa kwetu.
Salamu Maria...

K. Utuombee, Ee Mzazi Mtakatifu wa Mungu,
W. Tujaliwe ahadi za Kristo.

Tuombe:
Ee Bwana, tunakuomba umimine neema yako mioyoni mwetu,
ili sisi tuliopata kumjua Kristo Mwanao aliyefanyika mwili
kwa ujumbe wa Malaika,
kwa mateso na msalaba wake
tufikishwe katika utukufu wa ufufuko wake.
Kwa Kristo Bwana wetu. Amina.
            """.trimIndent()
        ),

        Prayer(
            id = "regina_caeli",
            title = "Regina Caeli — Malkia wa Mbinguni",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Sala ya wakati wa Pasaka",
            text = """
Malkia wa Mbinguni, furahi, aleluya!
Kwa maana yule uliyestahili kumbeba, aleluya,
amefufuka kama alivyosema, aleluya.

Tuombee kwa Mungu, aleluya.

Furahi na shangilia, Ee Maria, aleluya!
Kwa kuwa Bwana amefufuka kweli, aleluya!

Tuombe:
Ee Mungu, uliyejalia ulimwengu kushangilia
kwa ufufuko wa Mwanao Yesu Kristo,
kwa maombezi ya Mama yake Bikira Maria,
tujalie tufikie furaha ya uzima wa milele.
Kwa Kristo Bwana wetu. Amina.
            """.trimIndent()
        ),

        // ---------------------------------------------------------------------
        // ANGELS / SAINTS
        // ---------------------------------------------------------------------

        Prayer(
            id = "guardian_angel",
            title = "Sala kwa Malaika Mlinzi",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Sala ya ulinzi na uongozi",
            text = """
Malaika wa Mungu, mlinzi wangu mpendwa,
ambaye kwa wema wa Mungu nimekabidhiwa kwako,
niangazie, nilinde, uniongoze na unitawale.
Nisaidie kumjua na kumpenda Mungu,
kuepuka dhambi,
na kufuata njia ya wokovu.
Amina.
            """.trimIndent()
        ),

        Prayer(
            id = "st_michael_sw",
            title = "Sala kwa Mtakatifu Mikaeli Malaika Mkuu",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Sala ya ulinzi dhidi ya uovu",
            text = """
Ee Mtakatifu Mikaeli Malaika Mkuu,
tulinde katika mapambano;
uwe ulinzi wetu dhidi ya uovu na hila za Shetani.

Mungu na amkemee, tunakuomba kwa unyenyekevu;
na wewe, Mkuu wa majeshi ya mbinguni,
kwa nguvu za Mungu,
msukume kuzimu Shetani na roho wengine waovu
wanaozunguka duniani wakitafuta kuangamiza roho za watu.
Amina.
            """.trimIndent()
        ),

        Prayer(
            id = "st_joseph",
            title = "Sala kwa Mtakatifu Yosefu",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Kwa ulinzi wa familia na kazi",
            text = """
Ee Mtakatifu Yosefu,
mume mwaminifu wa Bikira Maria
na mlezi wa Yesu Kristo,
utuombee mbele za Mungu.

Tupatie moyo wa uaminifu,
unyenyekevu na bidii katika kazi.
Linda familia zetu,
wasaidie baba na mama,
walinde watoto,
na uwafariji wote wanaopitia shida.

Tufundishe kutimiza mapenzi ya Mungu
kwa moyo wa utiifu kama ulivyofanya wewe.
Amina.
            """.trimIndent()
        ),

        Prayer(
            id = "st_rita",
            title = "Sala kwa Mtakatifu Rita wa Kashia",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Kwa nyakati ngumu na mahitaji mazito",
            text = """
Ee Mtakatifu Rita,
mtumishi mwaminifu wa Kristo
na mlinzi wa wanaoteseka,
utuombee.

Mbele ya Mungu peleka mahitaji yetu,
hasa nia hii ninayoiweka moyoni mwangu:
[TAJA NIA YAKO]

Tupatie uvumilivu katika mateso,
hekima katika maamuzi,
msamaha katika maumivu,
na imani ya kuendelea kumtumaini Mungu.

Kwa maombezi yako,
Mungu atujalie yale yanayolingana na mapenzi yake.
Amina.
            """.trimIndent()
        ),

        Prayer(
            id = "st_jude",
            title = "Sala kwa Mtakatifu Yuda Tadeo",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Kwa mahitaji magumu na yasiyo na tumaini",
            text = """
Ee Mtakatifu Yuda Tadeo,
Mtume na rafiki mwaminifu wa Yesu,
utuombee.

Mungu anijalie neema ya kutokata tamaa,
bali kumtumaini katika kila hali.
Weka mbele zake hitaji langu:
[TAJA NIA YAKO]

Nisaidie kutafuta kwanza mapenzi ya Mungu,
na unipatie moyo wa uvumilivu,
imani na uaminifu.
Kwa Kristo Bwana wetu. Amina.
            """.trimIndent()
        ),

        Prayer(
            id = "st_anthony",
            title = "Sala kwa Mtakatifu Antoni wa Padua",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Kwa msaada na mahitaji ya maisha",
            text = """
Ee Mtakatifu Antoni,
mhubiri mwaminifu wa Injili
na mtumishi wa Kristo,
uniombee mbele za Mungu.

Nisaidie katika hitaji langu:
[TAJA NIA YAKO]

Niongoze nitafute ukweli,
nipende Neno la Mungu,
na niwe tayari kuwasaidia wengine.
Kwa maombezi yako,
Mungu anijalie neema inayofaa
kwa utukufu wake na wokovu wangu.
Amina.
            """.trimIndent()
        ),

        // ---------------------------------------------------------------------
        // MARIAN LITANY
        // ---------------------------------------------------------------------

        Prayer(
            id = "litany_of_loreto",
            title = "Litania ya Bikira Maria",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Litania ya Loreto",
            text = """
Bwana, utuhurumie.
Kristo, utuhurumie.
Bwana, utuhurumie.
Kristo, utusikie.
Kristo, utusikilize.

Baba wa mbinguni, Mungu, utuhurumie.
Mwana Mkombozi wa dunia, Mungu, utuhurumie.
Roho Mtakatifu, Mungu, utuhurumie.
Utatu Mtakatifu, Mungu mmoja, utuhurumie.

Maria Mtakatifu, utuombee.
Mzazi Mtakatifu wa Mungu, utuombee.
Bikira Mtakatifu wa mabikira, utuombee.
Mama wa Kristo, utuombee.
Mama wa Mungu, utuombee.
Mama wa neema ya Mungu, utuombee.
Mama safi sana, utuombee.
Mama mwenye usafi, utuombee.
Mama usiye na doa, utuombee.
Mama mpendelevu, utuombee.
Mama wa ajabu, utuombee.
Mama wa shauri jema, utuombee.
Mama wa Mwumba, utuombee.
Mama wa Mkombozi, utuombee.

Bikira mwenye hekima, utuombee.
Bikira mwenye heshima, utuombee.
Bikira mwenye sifa, utuombee.
Bikira mwenye nguvu, utuombee.
Bikira mwenye huruma, utuombee.
Bikira mwaminifu, utuombee.

Kioo cha haki, utuombee.
Kiti cha hekima, utuombee.
Sababu ya furaha yetu, utuombee.
Chombo cha neema, utuombee.
Chombo cha heshima, utuombee.
Chombo bora cha ibada, utuombee.
Waridi la fumbo, utuombee.
Mnara wa Daudi, utuombee.
Mnara wa pembe, utuombee.
Nyumba ya dhahabu, utuombee.
Sanduku la Agano, utuombee.
Mlango wa mbingu, utuombee.
Nyota ya asubuhi, utuombee.

Afya ya wagonjwa, utuombee.
Makimbilio ya wakosefu, utuombee.
Mfariji wa wenye huzuni, utuombee.
Msaada wa Wakristo, utuombee.

Malkia wa Malaika, utuombee.
Malkia wa Mababu, utuombee.
Malkia wa Manabii, utuombee.
Malkia wa Mitume, utuombee.
Malkia wa Mashahidi, utuombee.
Malkia wa Waungama, utuombee.
Malkia wa Mabikira, utuombee.
Malkia wa Watakatifu wote, utuombee.
Malkia uliyechukuliwa mimba bila dhambi ya asili, utuombee.
Malkia uliyechukuliwa mbinguni, utuombee.
Malkia wa Rozari Takatifu, utuombee.
Malkia wa familia, utuombee.
Malkia wa amani, utuombee.

Mwanakondoo wa Mungu, uondoaye dhambi za dunia,
utusamehe, Bwana.
Mwanakondoo wa Mungu, uondoaye dhambi za dunia,
utusikilize, Bwana.
Mwanakondoo wa Mungu, uondoaye dhambi za dunia,
utuhurumie.

K. Utuombee, Mzazi Mtakatifu wa Mungu.
W. Tujaliwe ahadi za Kristo.

Tuombe:
Ee Bwana Mungu, utujalie sisi watumishi wako
afya ya roho na ya mwili sikuzote.
Tuopolewe na mashaka ya sasa
kwa maombezi ya Maria Mtakatifu, Bikira daima,
ili tupate furaha ya milele.
Tunaomba hayo kwa njia ya Kristo Bwana wetu. Amina.
            """.trimIndent()
        ),

        // ---------------------------------------------------------------------
        // HOLY ROSARY — COMPLETE GUIDE
        // ---------------------------------------------------------------------

        Prayer(
            id = "holy_rosary_complete_sw",
            title = "Rozari Takatifu — Mwongozo Kamili",
            category = PrayerCategory.ROSARY,
            subtitle = "Sala zote, mafumbo 20 na mpangilio wa siku",
            text = """
ROZARI TAKATIFU

MAANDALIZI
Fanya Ishara ya Msalaba.
Taja nia zako za Rozari.
Sema Kanuni ya Imani.
Sema Baba Yetu moja.
Sema Salamu Maria tatu kwa Imani, Matumaini na Mapendo.
Sema Atukuzwe Baba.

KATIKA KILA FUMBO
1. Tangaza fumbo.
2. Tafakari kwa muda mfupi.
3. Sema Baba Yetu moja.
4. Sema Salamu Maria kumi.
5. Sema Atukuzwe Baba.
6. Sema Ee Yesu wangu.
Rudia kwa mafumbo yote matano.

JUMATATU NA JUMAMOSI — MAFUMBO YA FURAHA

Fumbo la Kwanza: Malaika Gabrieli anamletea Maria ujumbe.
Tunda: Unyenyekevu.

Fumbo la Pili: Maria anamtembelea Elisabeti.
Tunda: Upendo kwa jirani.

Fumbo la Tatu: Yesu anazaliwa Bethlehemu.
Tunda: Umaskini wa roho na upendo wa unyenyekevu.

Fumbo la Nne: Yesu anatolewa Hekaluni.
Tunda: Utii.

Fumbo la Tano: Yesu anapatikana Hekaluni.
Tunda: Kumtafuta na kumpata Kristo.

JUMANNE NA IJUMAA — MAFUMBO YA UCHUNGU

Fumbo la Kwanza: Yesu anasali katika bustani ya Getsemane.
Tunda: Kujutia dhambi na kujisalimisha kwa mapenzi ya Mungu.

Fumbo la Pili: Yesu anapigwa mijeledi.
Tunda: Usafi wa moyo na kujinyima.

Fumbo la Tatu: Yesu anavikwa taji la miiba.
Tunda: Ujasiri wa kimaadili.

Fumbo la Nne: Yesu anabeba msalaba.
Tunda: Uvumilivu katika mateso.

Fumbo la Tano: Yesu anasulubiwa na kufa msalabani.
Tunda: Kujikana nafsi na wokovu.

JUMATANO NA JUMAPILI — MAFUMBO YA UTUKUFU

Fumbo la Kwanza: Yesu anafufuka katika wafu.
Tunda: Imani.

Fumbo la Pili: Yesu anapaa mbinguni.
Tunda: Matumaini na kutamani uzima wa milele.

Fumbo la Tatu: Roho Mtakatifu anawashukia Mitume.
Tunda: Hekima na bidii ya kitume.

Fumbo la Nne: Maria anapalizwa mbinguni mwili na roho.
Tunda: Neema ya kufa katika hali ya neema.

Fumbo la Tano: Maria anavikwa taji Malkia wa mbingu na dunia.
Tunda: Uvumilivu hadi mwisho.

ALHAMISI — MAFUMBO YA MWANGA

Fumbo la Kwanza: Yesu anabatizwa katika mto Yordani.
Tunda: Kujifungua kwa Roho Mtakatifu.

Fumbo la Pili: Yesu anajidhihirisha katika arusi ya Kana.
Tunda: Kumwendea Yesu kwa njia ya Maria.

Fumbo la Tatu: Yesu anatangaza Ufalme wa Mungu na kuita watu kutubu.
Tunda: Toba na kumtumaini Mungu.

Fumbo la Nne: Yesu anageuka sura mlimani.
Tunda: Kutamani utakatifu.

Fumbo la Tano: Yesu anaweka Ekaristi Takatifu.
Tunda: Kumwabudu Kristo.

BAADA YA ROZARI
Sema Salve Regina.
Kisha unaweza kusali Litania ya Bikira Maria.
            """.trimIndent()
        ),

        // ---------------------------------------------------------------------
        // DIVINE MERCY CHAPLET
        // ---------------------------------------------------------------------

        Prayer(
            id = "divine_mercy_chaplet_sw",
            title = "Rozari ya Huruma ya Mungu",
            category = PrayerCategory.DIVINE_MERCY,
            subtitle = "Rozari ya Huruma ya Mungu — mwongozo kamili",
            text = """
ISHARA YA MSALABA

SALA YA KUANZA
Ee Damu na Maji yaliyotoka katika Moyo wa Yesu
kuwa chemchemi ya huruma kwetu,
nakuamini wewe.

Rudia mara tatu:
Ee Damu na Maji yaliyotoka katika Moyo wa Yesu
kuwa chemchemi ya huruma kwetu,
nakuamini wewe.

KABLA YA KILA KUMI
Baba yetu...

Salamu Maria...

Kanuni ya Imani...

KATIKA SHANGA KUBWA
Baba wa Milele,
nakutolea Mwili na Damu,
Roho na Umungu wa Mwanao mpendwa,
Bwana wetu Yesu Kristo,
kwa ajili ya malipizi ya dhambi zetu
na za ulimwengu wote.

KATIKA SHANGA KUMI
Kwa ajili ya mateso yake makali,
utuhurumie sisi na ulimwengu wote.

RUDIA
Shanga kubwa moja + shanga kumi,
mara tano.

MWISHO — MARA TATU
Mungu Mtakatifu,
Mungu Mwenye Nguvu,
Mungu Asiyekufa,
utuhurumie sisi na ulimwengu wote.

SALA YA KUFUNGA
Ee Mungu wa milele,
ambaye ndani yako huruma haina mwisho
na hazina ya huruma haiwezi kwisha,
utuangalie kwa wema
na utuongezee huruma yako,
ili katika nyakati ngumu tusikate tamaa
wala kukata moyo,
bali kwa tumaini kubwa
tujisalimishe kwa mapenzi yako matakatifu,
ambayo ni upendo na huruma yenyewe.
Amina.
            """.trimIndent()
        ),

        // ---------------------------------------------------------------------
        // ANIMA CHRISTI
        // ---------------------------------------------------------------------

        Prayer(
            id = "anima_christi_sw",
            title = "Anima Christi — Roho ya Kristo",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Sala baada ya Komunyo",
            text = """
Roho ya Kristo, unitakase.
Mwili wa Kristo, uniokoe.
Damu ya Kristo, nileweshe.
Maji yaliyotoka ubavuni mwa Kristo, unitakase.
Mateso ya Kristo, unitie nguvu.
Ee Yesu mwema, unisikie.
Ndani ya majeraha yako, unifiche.
Usiniruhusu nitengane nawe.
Unilinde na adui mwovu.
Wakati wa kufa kwangu, niite,
na uniambie nije kwako,
ili pamoja na watakatifu wako nikusifu
milele na milele. Amina.
            """.trimIndent()
        ),

        // ---------------------------------------------------------------------
        // EUCHARISTIC PRAYERS
        // ---------------------------------------------------------------------

        Prayer(
            id = "before_communion",
            title = "Sala Kabla ya Komunyo",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Maandalizi ya kumpokea Kristo katika Ekaristi",
            text = """
Ee Yesu wangu,
ninaamini kwamba wewe kweli upo katika Sakramenti Takatifu.
Nakupenda kwa moyo wangu wote.
Ninajutia dhambi zangu,
kwa sababu nimekukosea wewe uliye mwema.

Nakuomba uingie moyoni mwangu,
unitakase,
uniimarishe katika imani,
na unisaidie kuishi kama mfuasi wako mwaminifu.
Amina.
            """.trimIndent()
        ),

        Prayer(
            id = "after_communion",
            title = "Sala Baada ya Komunyo",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Shukrani baada ya kupokea Ekaristi",
            text = """
Ee Yesu wangu,
nakushukuru kwa zawadi ya Ekaristi Takatifu.
Asante kwa kuja moyoni mwangu.
Nisaidie nitunze neema hii
na niishi kwa upendo wako.

Nifanye niwe chombo cha amani,
msamaha na upendo kwa watu wote.
Uishi ndani yangu,
na mimi nibaki ndani yako.
Amina.
            """.trimIndent()
        ),

        Prayer(
            id = "eucharistic_adoration",
            title = "Sala ya Kuabudu Ekaristi",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Sala mbele ya Yesu katika Sakramenti Takatifu",
            text = """
Ee Yesu katika Ekaristi,
nakuabudu na ninakutambua kuwa Bwana na Mwokozi wangu.
Ninakushukuru kwa upendo wako
na kwa kujitoa kwako kwa ajili yetu.

Nipe moyo wa unyenyekevu,
imani iliyo hai,
na upendo unaotekelezwa.
Utakase moyo wangu
na unifanye kuwa mwaminifu katika mapenzi yako.
Amina.
            """.trimIndent()
        ),

        // ---------------------------------------------------------------------
        // MORNING / EVENING / NIGHT
        // ---------------------------------------------------------------------

        Prayer(
            id = "morning_offering",
            title = "Sala ya Asubuhi — Toleo la Siku",
            category = PrayerCategory.HOURS,
            subtitle = "Kumkabidhi Mungu siku nzima",
            text = """
Kwa Jina la Baba, na la Mwana, na la Roho Mtakatifu. Amina.

Ee Mungu wangu,
nakukabidhi siku hii yote:
mawazo yangu,
maneno yangu,
matendo yangu,
furaha zangu,
na changamoto zangu.

Niongoze katika kila jambo.
Nilinde nisitende dhambi.
Nifanye niwapende watu wote
na nitimize wajibu wangu kwa uaminifu.

Ee Maria, Mama wa Mungu,
uniombee.
Ee Mtakatifu Yosefu,
uniombee.
Malaika wangu Mlinzi,
nilinde.

Atukuzwe Baba, na Mwana, na Roho Mtakatifu,
kama mwanzo, na sasa, na siku zote,
na milele na milele. Amina.
            """.trimIndent()
        ),

        Prayer(
            id = "evening_thanksgiving",
            title = "Sala ya Jioni — Shukrani",
            category = PrayerCategory.HOURS,
            subtitle = "Shukrani na kujichunguza mwisho wa siku",
            text = """
Kwa Jina la Baba, na la Mwana, na la Roho Mtakatifu. Amina.

Ee Mungu wangu,
nakushukuru kwa siku hii.
Nakushukuru kwa maisha,
familia,
marafiki,
kazi,
chakula,
na kila neema uliyonipa.

Ninajichunguza mbele zako.
Kwa mema niliyotenda, nakushukuru.
Kwa mabaya niliyotenda, naomba msamaha.
Kwa mema niliyopaswa kutenda lakini sikuyatenda,
naomba unisamehe.

Nipe neema ya kuanza upya kesho
na moyo ulio tayari kufanya mapenzi yako.
Amina.
            """.trimIndent()
        ),

        Prayer(
            id = "night_prayer",
            title = "Sala ya Usiku",
            category = PrayerCategory.HOURS,
            subtitle = "Sala ya kabla ya kulala",
            text = """
Bwana, utujalie usiku wa amani na mwisho mwema. Amina.

Ee Mungu wangu,
nakushukuru kwa siku hii iliyopita.
Ninakuomba unisamehe dhambi zangu zote
na unisafishe moyo wangu.

Linda familia yangu,
wagonjwa,
wasafiri,
wanaoteseka,
na wote wanaohitaji msaada wako.

Malaika wa Mungu, mlinzi wangu,
niangazie, nilinde, uniongoze na unitawale.

Ee Bwana,
weka amani moyoni mwangu
na unipe usingizi salama.
Amina.
            """.trimIndent()
        ),

        Prayer(
            id = "examination_of_conscience",
            title = "Tafakari ya Dhamiri",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Kujichunguza kabla ya kulala au kabla ya Kitubio",
            text = """
Kaa kimya mbele za Mungu.

Jiulize:
Je, leo nimempenda Mungu kwa moyo wangu wote?
Je, nimetumia muda wangu kwa uaminifu?
Je, nimewaonyesha wengine upendo na heshima?
Je, nimesema uongo au kumdhuru mtu?
Je, nimekataa kusamehe?
Je, nimetenda dhambi kwa mawazo, maneno au matendo?
Je, nimeacha kufanya jambo jema nililopaswa kufanya?

Mshukuru Mungu kwa mema.
Omba msamaha kwa dhambi.
Azimia kubadilika na kuepuka nafasi za dhambi.
Kisha sema Tendo la Toba.
            """.trimIndent()
        ),

        // ---------------------------------------------------------------------
        // MEALS / FAMILY
        // ---------------------------------------------------------------------

        Prayer(
            id = "before_meals",
            title = "Sala Kabla ya Chakula",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Baraka ya chakula",
            text = """
Ee Mungu, Baba yetu,
tunakuomba ubariki chakula hiki
tunachokwenda kula,
na uwabariki wote waliokitoa na kukiandaa.

Tujalie tukitumia kwa shukrani,
na usiwakosee chakula wale wote wenye njaa.
Kwa Kristo Bwana wetu. Amina.
            """.trimIndent()
        ),

        Prayer(
            id = "after_meals",
            title = "Sala Baada ya Chakula",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Shukrani baada ya chakula",
            text = """
Tunashukuru, Ee Mungu Mwenyezi,
kwa chakula na kila neema uliyotupa.
Usaidie wenye njaa,
uwafariji wenye shida,
na utufanye sisi kuwa mikono yako
ya kuwasaidia wengine.
Kwa Kristo Bwana wetu. Amina.
            """.trimIndent()
        ),

        // ---------------------------------------------------------------------
        // HOLY SPIRIT
        // ---------------------------------------------------------------------

        Prayer(
            id = "come_holy_spirit",
            title = "Njoo Roho Mtakatifu",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Sala ya kumwomba Roho Mtakatifu",
            text = """
Njoo, Roho Mtakatifu,
ujaze mioyo ya waamini wako
na uwake ndani yao moto wa upendo wako.

Tuma Roho wako, nao wataumbwa,
na utaifanya upya sura ya dunia.

Tuombe:
Ee Mungu,
uliyeijaza mioyo ya waamini wako
kwa mwanga wa Roho Mtakatifu,
tujalie kwa Roho huyo huyo
tujue yaliyo mema
na kufurahia daima faraja zake.
Kwa Kristo Bwana wetu. Amina.
            """.trimIndent()
        ),

        Prayer(
            id = "vieni_creator_spiritus",
            title = "Veni Creator Spiritus",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Sala ya kumwomba Muumba Roho Mtakatifu",
            text = """
Njoo, Roho Muumba,
tembelea akili za watu wako,
ujaze kwa neema ya mbinguni
mioyo uliyoumba.

Unaitwa Mfariji,
zawadi ya Mungu Mkuu,
chemchemi ya uzima,
moto, upendo na upako wa kiroho.

Tawaza vipawa vyako,
utawale mioyo yetu,
na utufanye waaminifu kwa Kristo.
Amina.
            """.trimIndent()
        ),

        // ---------------------------------------------------------------------
        // SACRED HEART
        // ---------------------------------------------------------------------

        Prayer(
            id = "sacred_heart",
            title = "Sala kwa Moyo Mtakatifu wa Yesu",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Ibada kwa Moyo Mtakatifu wa Yesu",
            text = """
Ee Yesu, Moyo wako Mtakatifu,
nakuja mbele yako kwa imani na unyenyekevu.

Tawala moyo wangu,
nitakase kutokana na dhambi,
na unifundishe kupenda kama wewe.

Katika mahitaji yangu yote,
nisaidie kutafuta mapenzi ya Baba.
Katika mateso,
nipe uvumilivu.
Katika furaha,
nipe shukrani.
Katika kila jambo,
nipe uaminifu.

Moyo Mtakatifu wa Yesu,
nakuamini.
Moyo Mtakatifu wa Yesu,
utukuzwe katika maisha yangu.
Amina.
            """.trimIndent()
        ),

        Prayer(
            id = "immaculate_heart_mary",
            title = "Sala kwa Moyo Safi wa Maria",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Kujikabidhi kwa Mama wa Mungu",
            text = """
Ee Moyo Safi wa Maria,
Mama wa Mungu na Mama yetu,
utuombee na utuongoze kwa Yesu.

Utusaidie kuwa na moyo safi,
unyenyekevu,
uaminifu,
na upendo kwa Mungu na jirani.

Tukinge na dhambi,
tusaidie kutubu kwa kweli,
na utuongoze katika njia ya Kristo.
Amina.
            """.trimIndent()
        ),

        // ---------------------------------------------------------------------
        // TE DEUM
        // ---------------------------------------------------------------------

        Prayer(
            id = "te_deum_sw",
            title = "Te Deum — Tunakusifu, Ee Mungu",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Wimbo wa sifa na shukrani",
            text = """
Tunakuabudu, Ee Mungu;
tunakuungama kuwa wewe ndiye Bwana.

Dunia yote inakusifu,
Baba wa milele.

Malaika wote wanakusifu;
mbingu na nguvu zake zote,
makerubi na maserafi,
wanalia bila kukoma:

Mtakatifu, Mtakatifu, Mtakatifu,
Bwana Mungu wa majeshi.
Mbingu na dunia zimejaa utukufu wako.

Mitume wanakusifu;
manabii wanakusifu;
mashahidi wanakusifu.

Kanisa takatifu linakutangaza,
Baba mwenye enzi,
Mwana wako wa kweli na wa pekee,
na Roho wako Mtakatifu, Mfariji.

Wewe, Ee Kristo,
ni Mfalme wa utukufu.
Wewe ni Mwana wa milele wa Baba.

Wewe ulipochukua ubinadamu
hukukataa tumbo la Bikira.
Uliwashinda mauti
na kuwafungulia waamini ufalme wa mbinguni.

Tunakuamini kuwa utakuja kuwa hakimu.
Tusaidie sisi watumishi wako
uliowakomboa kwa damu yako ya thamani.

Wahesabu pamoja na watakatifu wako
katika utukufu wa milele.
Amina.
            """.trimIndent()
        ),

        // ---------------------------------------------------------------------
        // STATIONS OF THE CROSS
        // ---------------------------------------------------------------------

        Prayer(
            id = "stations_of_the_cross",
            title = "Njia ya Msalaba — Vituo 14",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Kutafakari mateso na kifo cha Bwana Yesu",
            text = """
UTANGULIZI
Kwa Jina la Baba, na la Mwana, na la Roho Mtakatifu. Amina.

Kabla ya kila kituo:
K. Ee Yesu, tunakuabudu na tunakushukuru.
W. Kwa kuwa kwa Msalaba wako Mtakatifu umeukomboa ulimwengu.

BAADA YA KILA KITUO
Baba yetu...
Salamu Maria...
Atukuzwe Baba...

KITUO CHA 1
Yesu anahukumiwa kifo.

KITUO CHA 2
Yesu anapewa msalaba wake.

KITUO CHA 3
Yesu anaanguka mara ya kwanza.

KITUO CHA 4
Yesu anakutana na Mama yake.

KITUO CHA 5
Simoni wa Kurene anamsaidia Yesu kubeba msalaba.

KITUO CHA 6
Veronika anampangusa Yesu uso.

KITUO CHA 7
Yesu anaanguka mara ya pili.

KITUO CHA 8
Yesu anawakuta wanawake wa Yerusalemu.

KITUO CHA 9
Yesu anaanguka mara ya tatu.

KITUO CHA 10
Yesu anavuliwa mavazi yake.

KITUO CHA 11
Yesu anasulubishwa msalabani.

KITUO CHA 12
Yesu anakufa msalabani.

KITUO CHA 13
Yesu anashushwa msalabani.

KITUO CHA 14
Yesu anawekwa kaburini.

MWISHO
Ee Yesu,
kwa mateso na kifo chako,
utusamehe dhambi zetu,
ututie nguvu kubeba misalaba yetu,
na utuongoze katika uzima wa milele.
Amina.
            """.trimIndent()
        ),

        // ---------------------------------------------------------------------
        // NOVENA — HOLY SPIRIT
        // ---------------------------------------------------------------------

        Prayer(
            id = "novena_holy_spirit",
            title = "Novena ya Roho Mtakatifu",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Siku 9 za kumwomba Roho Mtakatifu",
            text = """
NAMNA YA KUSALI
Sali kwa siku tisa mfululizo.
Kila siku:
1. Ishara ya Msalaba.
2. Taja nia yako.
3. Sema sala ya siku.
4. Sema Baba Yetu, Salamu Maria na Atukuzwe Baba.
5. Malizia kwa "Njoo Roho Mtakatifu."

SIKU YA 1 — ROHO WA KWELI
Ee Roho Mtakatifu,
tuangazie ili tujue ukweli wa Mungu.
Ondoa giza la ujinga na mashaka.
Tufanye waaminifu kwa Injili.
Amina.

SIKU YA 2 — ROHO WA HEKIMA
Ee Roho Mtakatifu,
tujalie hekima ya kuchagua mema,
kuepuka mabaya,
na kufanya maamuzi yanayompendeza Mungu.
Amina.

SIKU YA 3 — ROHO WA UFAHAMU
Ee Roho Mtakatifu,
tusaidie kuelewa Neno la Mungu
na kulifanya kuwa sehemu ya maisha yetu.
Amina.

SIKU YA 4 — ROHO WA NGUVU
Ee Roho Mtakatifu,
tuimarishe tunapokabili majaribu,
mateso na hofu.
Tufanye mashahidi jasiri wa Kristo.
Amina.

SIKU YA 5 — ROHO WA MAARIFA
Ee Roho Mtakatifu,
tufundishe kuona viumbe na mambo ya dunia
kwa mwanga wa Mungu.
Tusaidie kutumia vipawa vyetu kwa mema.
Amina.

SIKU YA 6 — ROHO WA UCHAJI
Ee Roho Mtakatifu,
weka ndani yetu heshima ya kweli kwa Mungu.
Tusaidie kumwabudu kwa moyo wote.
Amina.

SIKU YA 7 — ROHO WA USHAURI
Ee Roho Mtakatifu,
tuongoze katika maamuzi yetu.
Tufanye tusikie sauti ya dhamiri
na kufuata mapenzi ya Mungu.
Amina.

SIKU YA 8 — MATUNDA YA ROHO
Ee Roho Mtakatifu,
zalisha ndani yetu upendo,
furaha, amani, uvumilivu,
wema, fadhili, uaminifu,
upole na kiasi.
Amina.

SIKU YA 9 — ZAWADI YA ROHO
Ee Roho Mtakatifu,
ujalie Kanisa lako upya,
washa mioyo yetu kwa upendo,
na utufanye watumishi wa amani na Injili.
Amina.

SALA YA MWISHO
Njoo, Roho Mtakatifu,
ujaze mioyo ya waamini wako
na uwake ndani yao moto wa upendo wako.
Amina.
            """.trimIndent()
        ),

        // ---------------------------------------------------------------------
        // NOVENA — ST JOSEPH
        // ---------------------------------------------------------------------

        Prayer(
            id = "novena_st_joseph",
            title = "Novena ya Mtakatifu Yosefu",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Siku 9 za kumwomba Mtakatifu Yosefu",
            text = """
Kila siku anza:
Kwa Jina la Baba, na la Mwana, na la Roho Mtakatifu. Amina.

Ee Mtakatifu Yosefu,
mume wa Maria na mlezi wa Yesu,
tuombee na ulinde familia zetu.

NIA:
[TAJA NIA YAKO]

SIKU YA 1 — MUME MWAMINIFU
Tujalie moyo wa uaminifu katika ndoa,
familia na wito wetu.

SIKU YA 2 — MLEZI WA YESU
Linda watoto wetu na familia zetu.
Tufundishe malezi yenye upendo na hekima.

SIKU YA 3 — MTU WA KIMYA
Tufundishe kusikiliza Mungu
kabla ya kuzungumza na kutenda.

SIKU YA 4 — MFANYAKAZI
Bariki kazi za mikono yetu.
Wasaidie wasio na ajira na wanaotafuta riziki.

SIKU YA 5 — MTU WA HAKI
Tufanye waaminifu,
waadilifu na wenye huruma.

SIKU YA 6 — MLINZI WA KANISA
Mlinde Baba Mtakatifu,
maaskofu, mapadre, watawa na waamini wote.

SIKU YA 7 — MSAIDIZI WA WANAOTESKEA
Wafariji wagonjwa,
maskini, wakimbizi na wote walio katika shida.

SIKU YA 8 — PATRONI WA WANAOKUFA
Tuombee tupate neema ya kufa tukiwa katika urafiki na Mungu.

SIKU YA 9 — MTUMISHI WA MAPENZI YA MUNGU
Tufundishe kusema "ndiyo" kwa Mungu
na kutekeleza mapenzi yake kwa uaminifu.

MWISHO WA KILA SIKU
Baba Yetu...
Salamu Maria...
Atukuzwe Baba...

Ee Mtakatifu Yosefu,
utuombee ili tustahili ahadi za Kristo.
Amina.
            """.trimIndent()
        ),

        // ---------------------------------------------------------------------
        // NOVENA — DIVINE MERCY
        // ---------------------------------------------------------------------

        Prayer(
            id = "novena_divine_mercy",
            title = "Novena ya Huruma ya Mungu",
            category = PrayerCategory.DIVINE_MERCY,
            subtitle = "Siku 9 za kumwomba Mungu mwenye huruma",
            text = """
Kila siku:
Ishara ya Msalaba.
Taja nia yako.
Soma tafakari ya siku.
Sema Baba Yetu, Salamu Maria na Atukuzwe Baba.
Kisha sali Rozari ya Huruma ya Mungu.

SIKU YA 1
Tuombe kwa ajili ya watu wote,
hasa wenye dhambi,
ili watambue huruma ya Mungu
na warudi kwake kwa moyo wa toba.

SIKU YA 2
Tuombe kwa ajili ya mapadre na watawa,
ili wawe mashahidi wa huruma ya Kristo.

SIKU YA 3
Tuombe kwa ajili ya watu waaminifu,
ili wakue katika imani, matumaini na mapendo.

SIKU YA 4
Tuombe kwa ajili ya wale wasiomjua Mungu,
ili nuru ya Injili iwafikie.

SIKU YA 5
Tuombe kwa ajili ya Wakristo waliotengana,
ili wote wapate umoja katika Kristo.

SIKU YA 6
Tuombe kwa ajili ya watoto na vijana,
ili walindwe na uovu
na wakue katika imani.

SIKU YA 7
Tuombe kwa ajili ya watu wanaoteseka,
wagonjwa, maskini, wafungwa
na waliokata tamaa.

SIKU YA 8
Tuombe kwa ajili ya roho za wale waliofariki,
ili Mungu awapokee katika rehema yake.

SIKU YA 9
Tuombe kwa ajili ya ulimwengu mzima,
ili watu wote wapate kumjua Mungu
na kuishi katika amani.

SALA YA MWISHO
Ee Mungu mwenye huruma,
tunakuweka mbele zako mahitaji yetu yote.
Tufundishe kuamini upendo wako,
kusamehe,
na kuwa vyombo vya huruma yako.
Kwa Kristo Bwana wetu. Amina.
            """.trimIndent()
        ),

        // ---------------------------------------------------------------------
        // NOVENA — SACRED HEART
        // ---------------------------------------------------------------------

        Prayer(
            id = "novena_sacred_heart",
            title = "Novena ya Moyo Mtakatifu wa Yesu",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Siku 9 za ibada kwa Moyo Mtakatifu",
            text = """
Kila siku:
Ishara ya Msalaba.
Taja nia yako.
Soma sala ya siku.
Baba Yetu...
Salamu Maria...
Atukuzwe Baba...

SIKU YA 1 — UPENDO
Moyo Mtakatifu wa Yesu,
tufundishe kupenda kwa moyo safi.

SIKU YA 2 — HURUMA
Moyo Mtakatifu wa Yesu,
tupe huruma kwa wanaoteseka.

SIKU YA 3 — MSAMAHA
Moyo Mtakatifu wa Yesu,
tufundishe kusamehe kama wewe unavyosamehe.

SIKU YA 4 — UNYENYEKEVU
Moyo Mtakatifu wa Yesu,
ufanye moyo wetu uwe mpole na mnyenyekevu.

SIKU YA 5 — UVUMILIVU
Moyo Mtakatifu wa Yesu,
tuimarishe katika majaribu na mateso.

SIKU YA 6 — UAMINIFU
Moyo Mtakatifu wa Yesu,
tufanye waaminifu katika wajibu wetu.

SIKU YA 7 — FAMILIA
Moyo Mtakatifu wa Yesu,
ibariki familia zetu na uzifanye shule za upendo.

SIKU YA 8 — KANISA
Moyo Mtakatifu wa Yesu,
litakase na liimarishe Kanisa lako.

SIKU YA 9 — KUJITOA
Moyo Mtakatifu wa Yesu,
tunajikabidhi kwako.
Tawala maisha yetu
na utufanye vyombo vya amani yako.

SALA YA MWISHO
Ee Moyo Mtakatifu wa Yesu,
tunakuamini.
Uwe mfalme wa mioyo yetu
na utawale familia, Kanisa na ulimwengu.
Amina.
            """.trimIndent()
        ),

        // ---------------------------------------------------------------------
        // NOVENA — ST RITA
        // ---------------------------------------------------------------------

        Prayer(
            id = "novena_st_rita",
            title = "Novena ya Mtakatifu Rita wa Kashia",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Siku 9 za maombezi ya Mtakatifu Rita",
            text = """
Kila siku:
Ishara ya Msalaba.
Taja nia yako.
Ee Mtakatifu Rita, mwombezi katika hali ngumu,
utuombee.

SIKU YA 1 — IMANI
Tujalie imani isiyoyumba katika Mungu.

SIKU YA 2 — UVUMILIVU
Tujalie kuvumilia mateso bila kukata tamaa.

SIKU YA 3 — MSAMAHA
Tufundishe kusamehe kwa moyo wote.

SIKU YA 4 — FAMILIA
Ombea familia zetu amani, uaminifu na upendo.

SIKU YA 5 — WAGONJWA
Waombee wagonjwa na wanaoteseka.

SIKU YA 6 — MAHUSIANO YALIYOVUNJIKA
Ombea maridhiano na uponyaji wa mahusiano.

SIKU YA 7 — MAHITAJI MAGUMU
Wasaidie wote walio katika hali wanayoona haina njia.

SIKU YA 8 — UAMINIFU
Tusaidie kubaki waaminifu kwa Kristo katika kila hali.

SIKU YA 9 — MATUMAINI
Tufundishe kumtumaini Mungu hata wakati majibu
hayaji kwa namna tunayotarajia.

NIA YANGU:
[TAJA NIA]

MWISHO
Mtakatifu Rita,
utuombee mbele ya Kristo.
Baba Yetu...
Salamu Maria...
Atukuzwe Baba...
Amina.
            """.trimIndent()
        ),

        // ---------------------------------------------------------------------
        // SIMPLE DEVOTIONAL PRAYERS
        // ---------------------------------------------------------------------

        Prayer(
            id = "prayer_for_peace",
            title = "Sala ya Amani",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Kwa amani ya moyo, familia na dunia",
            text = """
Ee Mungu wa amani,
weka amani ndani ya mioyo yetu,
katika familia zetu,
katika Kanisa lako,
na katika nchi na ulimwengu.

Ondoa chuki, vurugu, kisasi na ubaguzi.
Tufanye wajenzi wa amani,
wa haki,
msamaha na upendo.
Kwa Kristo Bwana wetu. Amina.
            """.trimIndent()
        ),

        Prayer(
            id = "prayer_for_the_sick",
            title = "Sala kwa Wagonjwa",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Kwa wagonjwa na wanaoteseka",
            text = """
Ee Mungu mwenye huruma,
waangalie kwa upendo wote wagonjwa.

Wape nguvu katika udhaifu,
faraja katika maumivu,
tumaini katika hofu,
na afya ikiwa ni mapenzi yako.

Wabariki madaktari,
wauguzi,
walezi na familia zao.
Usiwaache wale wanaoteseka peke yao.
Kwa Kristo Bwana wetu. Amina.
            """.trimIndent()
        ),

        Prayer(
            id = "prayer_for_the_dead",
            title = "Sala kwa Waamini Waliokufa",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Kwa roho za marehemu",
            text = """
Ee Mungu,
uwapumzishe katika amani waamini wote waliokufa.

Mwanga wa milele uwaangazie.
Roho zao na roho za waamini wote waliokufa
kwa huruma ya Mungu zipumzike kwa amani.
Amina.
            """.trimIndent()
        ),

        Prayer(
            id = "prayer_for_family",
            title = "Sala kwa Familia",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Kujikabidhi familia kwa Mungu",
            text = """
Ee Mungu Baba,
ibariki familia yetu.
Tujalie upendo,
heshima,
uaminifu,
uvumilivu na msamaha.

Linda watoto wetu.
Wape wazazi hekima.
Wasaidie wanandoa kupendana na kusameheana.
Wasaidie wazee na wagonjwa.
Uifanye nyumba yetu iwe mahali pa amani
na sala.
Kwa Kristo Bwana wetu. Amina.
            """.trimIndent()
        ),

        Prayer(
            id = "prayer_for_travel",
            title = "Sala ya Safari",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Kabla ya kuanza safari",
            text = """
Ee Mungu,
tuongoze katika safari yetu.
Tulinde njiani,
tuokoe na ajali,
hatari na maovu.

Wabariki madereva na wasafiri wote.
Tufikishe salama tunakokwenda
na uturudishe salama.
Kwa Kristo Bwana wetu. Amina.
            """.trimIndent()
        ),

        Prayer(
            id = "prayer_before_work",
            title = "Sala Kabla ya Kazi",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Kumkabidhi Mungu kazi ya siku",
            text = """
Ee Mungu,
bariki kazi ninayoenda kufanya.
Nipe hekima,
bidii,
uaminifu na subira.

Nisaidie nisimuone mtu yeyote kuwa wa chini yangu,
bali niwaheshimu wote.
Nifanye kazi kwa uaminifu
na nitumie matunda yake kwa mema.
Amina.
            """.trimIndent()
        ),

        Prayer(
            id = "prayer_for_vocations",
            title = "Sala ya Miito",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Kwa miito ya upadre, utawa na maisha ya ndoa",
            text = """
Ee Mungu wa mavuno,
tuma watenda kazi katika mavuno yako.

Waita vijana wengi wakufuate
katika upadre,
utawa,
maisha ya kitume,
na wito wa ndoa na familia.

Wape ujasiri wa kuitikia mwito wako
na uwape walezi na jumuiya
zitakazowasaidia kukua katika wito wao.
Kwa Kristo Bwana wetu. Amina.
            """.trimIndent()
        ),

        Prayer(
            id = "prayer_for_pope",
            title = "Sala kwa Baba Mtakatifu",
            category = PrayerCategory.TRADITIONAL,
            subtitle = "Kwa ajili ya Baba Mtakatifu na Kanisa",
            text = """
Ee Mungu,
mlinde na umtie nguvu Baba Mtakatifu.
Mpe hekima,
afya,
ujasiri na upendo
katika kuliongoza Kanisa lako.

Msaidie awatumikie watu wa Mungu
kwa unyenyekevu na uaminifu.
Kwa Kristo Bwana wetu. Amina.
            """.trimIndent()
        )
    )
}
