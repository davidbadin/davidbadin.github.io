package sk.punkacidetom.pd2026.feature.info

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import sk.punkacidetom.pd2026.core.ui.theme.LocalAppSpacing
import sk.punkacidetom.pd2026.core.ui.theme.Navy
import sk.punkacidetom.pd2026.core.ui.theme.White
import sk.punkacidetom.pd2026.core.ui.theme.WhiteAlpha60
import sk.punkacidetom.pd2026.core.ui.theme.SHORT_HEADER_LOGO_TOP_PADDING_DP
import sk.punkacidetom.pd2026.core.ui.theme.SHORT_HEADER_LOGO_WIDTH_FRACTION
import sk.punkacidetom.pd2026.core.ui.theme.SHORT_HEADER_TITLE_BOTTOM_PADDING_DP
import sk.punkacidetom.pd2026.core.ui.theme.SHORT_HEADER_TITLE_TOP_PADDING_DP
import sk.punkacidetom.pd2026.core.ui.theme.PARALLAX_SCROLL_FRACTION

@Composable
fun InfoScreen(modifier: Modifier = Modifier) {
    val spacing = LocalAppSpacing.current
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Navy,
    ) { innerPadding ->

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Navy)
                .padding(innerPadding),
        ) {

            // Background image — fills Box
            AsyncImage(
                model = "file:///android_asset/header_main.png",
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { translationY = -scrollState.value * PARALLAX_SCROLL_FRACTION },
                alignment = Alignment.TopCenter,
            )

            // Single scrollable column — background image scrolls at 1× with content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Header area: Box sized by logo/title Column; background clipped to that height
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clipToBounds(),
                ) {
                    // Logo + title — sizes the Box; pushed below status bar
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(modifier = Modifier.height(SHORT_HEADER_LOGO_TOP_PADDING_DP.dp))
                        AsyncImage(
                            model = "file:///android_asset/logo_pd_short.png",
                            contentDescription = "Punkáči deťom 2026",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.fillMaxWidth(SHORT_HEADER_LOGO_WIDTH_FRACTION),
                        )
                        Spacer(modifier = Modifier.height(SHORT_HEADER_TITLE_TOP_PADDING_DP.dp))
                        Text(
                            text = stringResource(R.string.info_title),
                            style = MaterialTheme.typography.displayMedium,
                            color = White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.md),
                        )
                        Spacer(modifier = Modifier.height(SHORT_HEADER_TITLE_BOTTOM_PADDING_DP.dp))
                    }
                }

                // Screen content — info sections as native Compose
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.md),
                ) {
                    InfoContent()
                }
            }
        }
    }
}

@Composable
private fun InfoContent() {
    val spacing = LocalAppSpacing.current

    InfoSection(heading = "Festivalový areál") {
        InfoParagraph("Festivalový areál je otvorený od štvrtka 27. 8. od 13:00 do nedele 30. 8. do 11:00.")
        InfoParagraph("V areáli nájdeš:")
        InfoBullet("toalety")
        InfoBullet("cisterny s pitnou vodou")
        InfoBullet("stánky s občerstvením")
        InfoBullet("festivalový merch")
        InfoBullet("zdravotnú službu a hasičov (pri United Stage)")
        InfoParagraph("S platnou festivalovou páskou môžeš areál počas festivalu ľubovoľne opúšťať a opäť sa vracať. Festivalový náramok je neprenosný – pri poškodení alebo odstránení sa stáva neplatným.")
    }

    InfoSection(heading = "Bezpečnosť") {
        InfoParagraph("Osoba, ktorá vážne narúša priebeh festivalu alebo ohrozuje ostatných návštevníkov, môže byť z areálu vykázaná bez nároku na vrátenie vstupného.")
        InfoParagraph("Festival sa koná za každého počasia, pokiaľ meteorologické alebo bezpečnostné podmienky neohrozujú priebeh podujatia. Organizátor si vyhradzuje právo na zmenu programu.")
        InfoParagraph("Sleduj upozornenia v aplikácii a rešpektuj pokyny organizátorov a bezpečnostnej služby.")
    }

    InfoSection(heading = "Zakázané predmety") {
        InfoParagraph("Do areálu je zakázané vnášať:")
        InfoBullet("zbrane")
        InfoBullet("sklenené fľaše a nádoby")
        InfoBullet("dáždniky")
        InfoBullet("pyrotechniku")
        InfoBullet("toxické a omamné látky")
        InfoBullet("iné nebezpečné predmety")
        InfoParagraph("Zakázané je používanie vlastných dronov.")
        InfoParagraph("Fotografovanie mobilným telefónom alebo osobným fotoaparátom je povolené. Profesionálne fotografovanie a natáčanie bez súhlasu organizátora nie je povolené.")
        InfoParagraph("V celom areáli festivalu aj stanovom mestečku je prísne zakázané zakladať otvorený oheň.")
    }

    InfoSection(heading = "Psy") {
        InfoParagraph("Vstup so psami nie je povolený.")
        InfoParagraph("Festival je hlučné prostredie s veľkým množstvom ľudí, preto nie je pre zvieratá vhodný. Ďakujeme za pochopenie.")
    }

    InfoSection(heading = "Hygiena a sprchy") {
        InfoParagraph("Chemické toalety sú rozmiestnené vo festivalovom areáli aj v stanovom mestečku a každé dopoludnie prebieha ich čistenie.")
        InfoParagraph("Vedľa hlavného pódia sa nachádzajú murované toalety a sprchy.")
        InfoParagraph("Sprchy sú spoplatnené. Bezplatný vstup majú:")
        InfoBullet("držitelia VIP vstupeniek")
        InfoBullet("kapely")
        InfoBullet("crew")
        InfoParagraph("Neobmedzený vstup do spŕch je možné zakúpiť priamo na mieste.")
        InfoSubheading("Otváracie hodiny spŕch:")
        InfoParagraph("Štvrtok: 15:00 – 23:00")
        InfoParagraph("Piatok: 08:00 – 23:00")
        InfoParagraph("Sobota: 08:00 – 23:00")
    }

    InfoSection(heading = "Ekológia") {
        InfoParagraph("Prosíme, pomôž nám udržať festival čistý.")
        InfoParagraph("Na festivale sú rozmiestnené odpadkové koše. Návštevníci stanového mestečka si môžu pri vstupe bezplatne vyzdvihnúť vrecia na odpad, ktoré po naplnení odovzdajú do zberných nádob.")
        InfoParagraph("Nápoje sa vydávajú do zálohovaných vratných pohárov.")
    }

    InfoSection(heading = "Vstup") {
        InfoParagraph("Pri prvom vstupe si priprav platnú vstupenku alebo QR kód.")
        InfoParagraph("Po kontrole dostaneš festivalový náramok.")
        InfoParagraph("Vstupenky je možné kúpiť:")
        InfoBullet("online cez GoOut")
        InfoBullet("na pokladni festivalu každý deň (do 22:00)")
        InfoParagraph("Kapacita festivalu je obmedzená.")
    }

    InfoSection(heading = "Stanovanie") {
        InfoParagraph("Stanovanie je bezplatné.")
        InfoParagraph("Stanovať je možné iba v stanovom mestečku.")
        InfoParagraph("Stanovanie na parkovisku ani pri karavanoch nie je povolené.")
    }

    InfoSection(heading = "Straty a nálezy") {
        InfoParagraph("Straty a nálezy nájdeš pri hlavnom vstupe, kde sa nachádza predaj vstupeniek (otvorené do 22:00).")
    }

    InfoSection(heading = "Príchod autom") {
        InfoParagraph("Do navigácie zadaj:")
        InfoSubheading("Dalitrans Veľké Bierovce")
        InfoParagraph("Približne 150 metrov pred areálom odboč vľavo a pokračuj cez betónovú plochu (výkup paliet). Parkovanie je až priamo pri campingu.")
        InfoSubheading("GPS: 48.856780, 17.963672")
    }

    InfoSection(heading = "Taxi") {
        InfoParagraph("Ak prídeš taxíkom, požiadaj vodiča, aby ťa vysadil pri Dalitrans Veľké Bierovce.")
        InfoParagraph("Nevystupuj pri VIP bráne, odkiaľ je to pešo približne 1 km.")
        InfoParagraph("Na festivalovom parkovisku budú pristavené zazmluvnené festivalové taxíky za vopred zverejnené ceny.")
        InfoParagraph("Využiť môžeš aj službu Bolt.")
    }

    InfoSection(heading = "VIP a kapely") {
        InfoParagraph("Do navigácie zadajte:")
        InfoSubheading("Chata na rybníku Opatovce")
        InfoParagraph("Nezadávajte Farma ryby Opatovce — navigácia vás odvedie na opačnú stranu kanála a cesta do areálu bude výrazne dlhšia.")
    }

    InfoSection(heading = "Parkovanie") {
        InfoParagraph("Parkovisko je otvorené od štvrtka 12:00 do nedele 11:00.")
        InfoParagraph("Parkovanie je spoplatnené na celý festival.")
        InfoParagraph("Po zaplatení dostane každé vozidlo parkovaciu nálepku.")
        InfoParagraph("Organizátor nezodpovedá za veci ponechané vo vozidle.")
    }

    InfoSection(heading = "Kúpanie") {
        InfoParagraph("Kúpanie v jazerách campingu je prísne zakázané.")
        InfoParagraph("Jazerá sú ohradené. Porušenie zákazu môže viesť k odobratiu festivalového náramku bez nároku na náhradu.")
    }

    InfoSection(heading = "Deti na festivale") {
        InfoParagraph("Festival je priateľský k rodinám s deťmi.")
        InfoParagraph("Za bezpečnosť dieťaťa počas celej návštevy zodpovedá rodič alebo sprevádzajúca dospelá osoba.")
        InfoParagraph("Odporúčame používať chrániče sluchu, najmä v blízkosti pódií.")
        InfoParagraph("Prosíme návštevníkov o ohľaduplnosť voči deťom.")
    }

    InfoSection(heading = "Platby") {
        InfoParagraph("Na festivale sa platí prostredníctvom systému NFCtron.")
        InfoParagraph("Hotovosť prijíma iba festivalový merch stánok.")
    }
}

@Composable
private fun InfoSection(heading: String, content: @Composable () -> Unit) {
    val spacing = LocalAppSpacing.current
    Spacer(modifier = Modifier.height(spacing.lg))
    Text(
        text = heading,
        style = MaterialTheme.typography.headlineSmall,
        color = White,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(spacing.sm))
    content()
}

@Composable
private fun InfoSubheading(text: String) {
    val spacing = LocalAppSpacing.current
    Spacer(modifier = Modifier.height(spacing.xs))
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = White,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun InfoParagraph(text: String) {
    val spacing = LocalAppSpacing.current
    Spacer(modifier = Modifier.height(spacing.xs))
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = WhiteAlpha60,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun InfoBullet(text: String) {
    val spacing = LocalAppSpacing.current
    Spacer(modifier = Modifier.height(spacing.xs))
    Text(
        text = "• $text",
        style = MaterialTheme.typography.bodyMedium,
        color = WhiteAlpha60,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = spacing.md),
    )
}
