package sk.punkacidetom.pd2026.feature.info

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import sk.punkacidetom.pd2026.core.ui.theme.Crimson
import sk.punkacidetom.pd2026.core.ui.theme.LocalAppSpacing
import sk.punkacidetom.pd2026.core.ui.theme.Navy
import sk.punkacidetom.pd2026.core.ui.theme.NavyLight
import sk.punkacidetom.pd2026.core.ui.theme.PARALLAX_SCROLL_FRACTION
import sk.punkacidetom.pd2026.core.ui.theme.SHORT_HEADER_LOGO_TOP_PADDING_DP
import sk.punkacidetom.pd2026.core.ui.theme.SHORT_HEADER_LOGO_WIDTH_FRACTION
import sk.punkacidetom.pd2026.core.ui.theme.SHORT_HEADER_TITLE_BOTTOM_PADDING_DP
import sk.punkacidetom.pd2026.core.ui.theme.SHORT_HEADER_TITLE_TOP_PADDING_DP
import sk.punkacidetom.pd2026.core.ui.theme.White
import sk.punkacidetom.pd2026.core.ui.theme.WhiteAlpha60

private const val INFO_BACK_SWIPE_THRESHOLD_DP = 80

private enum class InfoChapter(val title: String) {
    FESTIVAL_AREA("Festivalový areál"),
    SAFETY("Bezpečnosť"),
    HYGIENE("Hygiena a sprchy"),
    ECOLOGY("Ekológia"),
    ENTRY_AND_CAMPING("Vstup a stanovanie"),
    LOST_AND_FOUND("Straty a nálezy"),
    ARRIVAL("Príchod"),
    SWIMMING("Kúpanie"),
    CHILDREN("Deti na festivale"),
    PAYMENTS("Platby"),
}

@Composable
fun InfoScreen(modifier: Modifier = Modifier) {
    val spacing     = LocalAppSpacing.current
    val scrollState = rememberScrollState()
    val density     = LocalDensity.current
    val backSwipeThresholdPx = with(density) { INFO_BACK_SWIPE_THRESHOLD_DP.dp.toPx() }
    var swipeAccumulator by remember { mutableFloatStateOf(0f) }
    var selectedChapter by remember { mutableStateOf<InfoChapter?>(null) }

    LaunchedEffect(selectedChapter) {
        scrollState.scrollTo(0)
    }

    BackHandler(enabled = selectedChapter != null) {
        selectedChapter = null
    }

    Scaffold(
        containerColor = Navy,
        contentWindowInsets = WindowInsets(0),
    ) { innerPadding ->

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Navy)
                .padding(innerPadding),
        ) {

            // Background image — parallax: translates up as the Column scrolls down
            AsyncImage(
                model = "file:///android_asset/header_main.png",
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { translationY = -scrollState.value * PARALLAX_SCROLL_FRACTION },
                alignment = Alignment.TopCenter,
            )

            // Single scrollable column — logo, title, and content all scroll together
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .pointerInput(selectedChapter) {
                        if (selectedChapter != null) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (swipeAccumulator > backSwipeThresholdPx) selectedChapter = null
                                    swipeAccumulator = 0f
                                },
                                onDragCancel = { swipeAccumulator = 0f },
                                onHorizontalDrag = { _, dragAmount ->
                                    swipeAccumulator += dragAmount
                                },
                            )
                        }
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                // Header area: clipped Box so the parallax image behind it is cropped at the boundary
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clipToBounds(),
                ) {
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

                AnimatedContent(
                    targetState = selectedChapter,
                    transitionSpec = {
                        val goingForward = targetState != null
                        (slideInHorizontally(initialOffsetX = { if (goingForward) it else -it }) + fadeIn()) togetherWith
                            (slideOutHorizontally(targetOffsetX = { if (goingForward) -it else it }) + fadeOut())
                    },
                    label = "InfoChapterTransition",
                    modifier = Modifier.fillMaxWidth(),
                ) { chapter ->
                    if (chapter == null) {
                        InfoChapterList(
                            onChapterSelect = { selectedChapter = it },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        InfoChapterContent(
                            chapter = chapter,
                            onBack = { selectedChapter = null },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(spacing.md))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Chapter navigation helpers — used only by InfoScreen
// ---------------------------------------------------------------------------

@Composable
private fun InfoChapterList(
    onChapterSelect: (InfoChapter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current
    Column(
        modifier = modifier.padding(horizontal = spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        InfoChapter.values().forEach { chapter ->
            InfoChapterRow(chapter = chapter, onClick = { onChapterSelect(chapter) })
        }
    }
}

@Composable
private fun InfoChapterRow(chapter: InfoChapter, onClick: () -> Unit) {
    val spacing = LocalAppSpacing.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.cardCorner))
            .background(NavyLight)
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.md, vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = chapter.title,
            style = MaterialTheme.typography.titleMedium,
            color = White,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = ">",
            style = MaterialTheme.typography.titleMedium,
            color = White,
        )
    }
}

@Composable
private fun InfoChapterContent(
    chapter: InfoChapter,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current
    Column(
        modifier = modifier.padding(horizontal = spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (chapter) {

            InfoChapter.FESTIVAL_AREA -> {
                InfoH2("Festivalový areál")
                InfoParagraph(buildAnnotatedString {
                    append("Festivalový areál je otvorený od ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = White)) { append("štvrtka 27. 8. od 13:00") }
                    append(" do ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = White)) { append("nedele 30. 8. do 11:00") }
                    append(".")
                })
                InfoParagraph("V areáli nájdeš:")
                InfoBullet("toalety")
                InfoBullet("cisterny s pitnou vodou")
                InfoBullet("stánky s občerstvením")
                InfoBullet("festivalový merch")
                InfoBullet("zdravotnú službu a hasičov (pri United Stage)")
                InfoParagraph("S platnou festivalovou páskou môžeš areál počas festivalu ľubovoľne opúšťať a opäť sa vracať. Festivalový náramok je neprenosný – pri poškodení alebo odstránení sa stáva neplatným.")
            }

            InfoChapter.SAFETY -> {
                InfoH2("Bezpečnosť")
                InfoParagraph("Osoba, ktorá vážne narúša priebeh festivalu alebo ohrozuje ostatných návštevníkov, môže byť z areálu vykázaná bez nároku na vrátenie vstupného.")
                InfoParagraph("Festival sa koná za každého počasia, pokiaľ meteorologické alebo bezpečnostné podmienky neohrozujú priebeh podujatia. Organizátor si vyhradzuje právo na zmenu programu.")
                InfoParagraph("Sleduj upozornenia v aplikácii a rešpektuj pokyny organizátorov a bezpečnostnej služby.")

                InfoH2("Zakázané predmety")
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

                InfoH2("Psy")
                InfoParagraph("Vstup so psami nie je povolený.")
                InfoParagraph("Festival je hlučné prostredie s veľkým množstvom ľudí, preto nie je pre zvieratá vhodný. Ďakujeme za pochopenie.")
            }

            InfoChapter.HYGIENE -> {
                InfoH2("Hygiena a sprchy")
                InfoParagraph("Chemické toalety sú rozmiestnené vo festivalovom areáli aj v stanovom mestečku a každé dopoludnie prebieha ich čistenie.")
                InfoParagraph("Vedľa hlavného pódia sa nachádzajú murované toalety a sprchy.")
                InfoParagraph("Sprchy sú spoplatnené. Bezplatný vstup majú:")
                InfoBullet("držitelia VIP vstupeniek")
                InfoBullet("kapely")
                InfoBullet("crew")
                InfoParagraph("Neobmedzený vstup do spŕch je možné zakúpiť priamo na mieste.")
                InfoParagraph(buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = White)) { append("Otváracie hodiny spŕch:") }
                })
                InfoParagraph("Štvrtok: 15:00 – 23:00")
                InfoParagraph("Piatok: 08:00 – 23:00")
                InfoParagraph("Sobota: 08:00 – 23:00")
            }

            InfoChapter.ECOLOGY -> {
                InfoH2("Ekológia")
                InfoParagraph("Prosíme, pomôž nám udržať festival čistý.")
                InfoParagraph("Na festivale sú rozmiestnené odpadkové koše. Návštevníci stanového mestečka si môžu pri vstupe bezplatne vyzdvihnúť vrecia na odpad, ktoré po naplnení odovzdajú do zberných nádob.")
                InfoParagraph("Nápoje sa vydávajú do zálohovaných vratných pohárov.")
            }

            InfoChapter.ENTRY_AND_CAMPING -> {
                InfoH2("Vstup")
                InfoParagraph("Pri prvom vstupe si priprav platnú vstupenku alebo QR kód.")
                InfoParagraph("Po kontrole dostaneš festivalový náramok.")
                InfoParagraph("Vstupenky je možné kúpiť:")
                InfoBullet("online cez GoOut")
                InfoBullet("na pokladni festivalu každý deň (do 22:00)")
                InfoParagraph("Kapacita festivalu je obmedzená.")

                InfoH2("Stanovanie")
                InfoParagraph("Stanovanie je bezplatné.")
                InfoParagraph("Stanovať je možné iba v stanovom mestečku.")
                InfoParagraph("Stanovanie na parkovisku ani pri karavanoch nie je povolené.")
            }

            InfoChapter.LOST_AND_FOUND -> {
                InfoH2("Straty a nálezy")
                InfoParagraph("Straty a nálezy nájdeš pri hlavnom vstupe, kde sa nachádza predaj vstupeniek (otvorené do 22:00).")
            }

            InfoChapter.ARRIVAL -> {
                InfoH2("Príchod autom")
                InfoParagraph("Do navigácie zadaj:")
                InfoParagraph(buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = White)) { append("Dalitrans Veľké Bierovce") }
                })
                InfoParagraph("Približne 150 metrov pred areálom odboč vľavo a pokračuj cez betónovú plochu (výkup paliet). Parkovanie je až priamo pri campingu.")
                InfoParagraph(buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = White)) { append("GPS:") }
                    append(" 48.856780, 17.963672")
                })

                InfoH2("Taxi")
                InfoParagraph(buildAnnotatedString {
                    append("Ak prídeš taxíkom, požiadaj vodiča, aby ťa vysadil pri ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = White)) { append("Dalitrans Veľké Bierovce") }
                    append(".")
                })
                InfoParagraph("Nevystupuj pri VIP bráne, odkiaľ je to pešo približne 1 km.")
                InfoParagraph("Na festivalovom parkovisku budú pristavené zazmluvnené festivalové taxíky za vopred zverejnené ceny.")
                InfoParagraph("Využiť môžeš aj službu Bolt.")

                InfoH2("VIP a kapely")
                InfoParagraph("Do navigácie zadajte:")
                InfoParagraph(buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = White)) { append("Chata na rybníku Opatovce") }
                })
                InfoParagraph(buildAnnotatedString {
                    append("Nezadávajte ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = White)) { append("Farma ryby Opatovce") }
                    append(" — navigácia vás odvedie na opačnú stranu kanála a cesta do areálu bude výrazne dlhšia.")
                })

                InfoH2("Parkovanie")
                InfoParagraph("Parkovisko je otvorené od štvrtka 12:00 do nedele 11:00.")
                InfoParagraph("Parkovanie je spoplatnené na celý festival.")
                InfoParagraph("Po zaplatení dostane každé vozidlo parkovaciu nálepku.")
                InfoParagraph("Organizátor nezodpovedá za veci ponechané vo vozidle.")
            }

            InfoChapter.SWIMMING -> {
                InfoH2("Kúpanie")
                InfoParagraph("Kúpanie v jazerách campingu je prísne zakázané.")
                InfoParagraph("Jazerá sú ohradené. Porušenie zákazu môže viesť k odobratiu festivalového náramku bez nároku na náhradu.")
            }

            InfoChapter.CHILDREN -> {
                InfoH2("Deti na festivale")
                InfoParagraph("Festival je priateľský k rodinám s deťmi.")
                InfoParagraph("Za bezpečnosť dieťaťa počas celej návštevy zodpovedá rodič alebo sprevádzajúca dospelá osoba.")
                InfoParagraph("Odporúčame používať chrániče sluchu, najmä v blízkosti pódií.")
                InfoParagraph("Prosíme návštevníkov o ohľaduplnosť voči deťom.")
            }

            InfoChapter.PAYMENTS -> {
                InfoH2("Platby")
                InfoParagraph(buildAnnotatedString {
                    append("Na festivale sa platí prostredníctvom systému ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = White)) { append("NFCtron") }
                    append(".")
                })
                InfoParagraph("Hotovosť prijíma iba festivalový merch stánok.")
            }
        }

        Spacer(modifier = Modifier.height(spacing.md))
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = White,
                contentColor = Navy,
            ),
            modifier = Modifier.fillMaxWidth(0.4f),
        ) {
            Text(
                text = "SPÄŤ",
                style = MaterialTheme.typography.labelLarge,
            )
        }
        Spacer(modifier = Modifier.height(spacing.md))
    }
}

/** H2-level section heading (BebasNeue, Crimson). Maps to HTML <h2>. */
@Composable
private fun InfoH2(text: String) {
    val spacing = LocalAppSpacing.current
    Spacer(modifier = Modifier.height(spacing.md))
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        color = Crimson,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(spacing.xs))
}

/** Body paragraph (Poppins, WhiteAlpha60). Maps to HTML <p>. */
@Composable
private fun InfoParagraph(text: AnnotatedString) {
    val spacing = LocalAppSpacing.current
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = WhiteAlpha60,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = spacing.xs),
    )
}

/** Convenience overload for plain-text paragraphs with no bold spans. */
@Composable
private fun InfoParagraph(text: String) = InfoParagraph(AnnotatedString(text))

/** Bullet list item. Maps to HTML <li>. */
@Composable
private fun InfoBullet(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, start = LocalAppSpacing.current.sm),
    ) {
        Text(text = "•  ", style = MaterialTheme.typography.bodyMedium, color = WhiteAlpha60)
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = WhiteAlpha60,
            modifier = Modifier.weight(1f),
        )
    }
}
