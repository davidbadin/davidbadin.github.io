package sk.punkacidetom.pd2026.feature.nfctron

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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import sk.punkacidetom.pd2026.core.ui.theme.Crimson
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
fun NfctronScreen(modifier: Modifier = Modifier) {
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
                            text = stringResource(R.string.nfctron_title),
                            style = MaterialTheme.typography.displayMedium,
                            color = White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.md),
                        )
                        Spacer(modifier = Modifier.height(SHORT_HEADER_TITLE_BOTTOM_PADDING_DP.dp))
                    }
                }

                // Screen content — NFCtron sections as native Compose
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.md),
                ) {
                    NfctronContent()
                }
            }
        }
    }
}

@Composable
private fun NfctronContent() {
    val spacing = LocalAppSpacing.current

    // Intro
    NfctronParagraph(
        buildAnnotatedString {
            append("Na festivale ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = White)) { append("Punkáči deťom") }
            append(" zaplatíš za jedlo, nápoje aj ďalšie služby jednoducho pomocou ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = White)) { append("NFCtron") }
            append(".")
        }
    )
    NfctronParagraph("Pri vstupe dostaneš festivalový náramok s čipom, ktorým zaplatíš jednoduchým priložením k terminálu.")
    NfctronParagraph("Dobíjacie miesto je v areáli otvorené: Štvrtok 13:00–02:00, Piatok 09:00–02:00, Sobota 09:00–04:00. Na dobíjacom mieste sa nachádza aj výkup kelímkov.")

    // Novinka 2026
    Spacer(modifier = Modifier.height(spacing.lg))
    Text(
        text = "Novinka 2026",
        style = MaterialTheme.typography.headlineSmall,
        color = Crimson,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(spacing.sm))
    NfctronParagraph(
        buildAnnotatedString {
            append("Počas festivalu si môžeš kredit ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = White)) { append("dobíjať aj priamo cez mobilnú aplikáciu NFCtron") }
            append(", takže už nemusíš chodiť na dobíjacie miesto. Stačí mať spárovaný náramok a podporu NFC vo svojom telefóne.")
        }
    )

    // Ako si dobiť kredit
    NfctronSection(heading = "Ako si dobiť kredit") {
        NfctronSubheading("Online pred festivalom (odporúčame)")
        NfctronParagraph(
            buildAnnotatedString {
                append("Kredit si môžeš dobiť ešte pred príchodom cez ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = White)) { append("NFCtron Tickets") }
                append(".")
            }
        )
        NfctronParagraph("Výhody:")
        NfctronBullet("bez aktivačného poplatku")
        NfctronBullet("rýchlejší vstup")
        NfctronBullet("prvá platba bez čakania")

        Spacer(modifier = Modifier.height(spacing.sm))
        NfctronSubheading("Cez mobilnú aplikáciu NFCtron (NOVINKA)")
        NfctronParagraph("Po aktivácii náramku si môžeš počas festivalu kredit dobíjať priamo v aplikácii NFCtron. Kredit sa na náramok pripíše priebežne a nemusíš navštíviť dobíjacie miesto.")

        Spacer(modifier = Modifier.height(spacing.sm))
        NfctronSubheading("Na dobíjacom mieste")
        NfctronParagraph("Kredit si môžeš dobiť aj na označenom NFCtron dobíjacom mieste.")
        NfctronParagraph("Možnosti platby:")
        NfctronBullet("hotovosť")
        NfctronBullet("platobná karta")
        NfctronParagraph(
            buildAnnotatedString {
                append("Pri prvom dobití na mieste sa účtuje ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = White)) { append("aktivačný poplatok 1,50 €") }
                append(".")
            }
        )
    }

    // Mobilná aplikácia
    NfctronSection(heading = "Mobilná aplikácia NFCtron") {
        NfctronParagraph(
            buildAnnotatedString {
                append("Odporúčame stiahnuť si aplikáciu ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = White)) { append("NFCtron") }
                append(" pre Android alebo iPhone.")
            }
        )
        NfctronParagraph("V aplikácii môžeš:")
        NfctronBullet("dobíjať kredit počas festivalu")
        NfctronBullet("sledovať aktuálny zostatok")
        NfctronBullet("prezerať históriu platieb")
        NfctronBullet("dostávať upozornenia o platbách")
        NfctronBullet("hodnotiť predajcov")
        NfctronBullet("po skončení festivalu jednoducho požiadať o vrátenie zostávajúceho kreditu")
    }

    // Vrátenie zostatku
    NfctronSection(heading = "Vrátenie zostatku") {
        NfctronParagraph("Ak celý kredit neminieš, o peniaze neprídeš.")
        NfctronParagraph(
            buildAnnotatedString {
                append("O vrátenie zostatku môžeš bezplatne požiadať ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = White)) { append("do 14 dní po skončení festivalu") }
                append(". Peniaze budú zaslané na tvoj bankový účet.")
            }
        )
    }

    // FAQ
    NfctronSection(heading = "Často kladené otázky") {
        NfctronSubheading("Ako zistím zostatok?")
        NfctronBullet("po každom nákupe u predajcu")
        NfctronBullet("priamo v aplikácii NFCtron")

        Spacer(modifier = Modifier.height(spacing.sm))
        NfctronSubheading("Potrebujem hotovosť?")
        NfctronParagraph("Nie. Väčšina platieb prebieha cez NFCtron. Hotovosť využiješ len na dobitie kreditu na dobíjacom mieste alebo vo festivalovom merch stánku.")

        Spacer(modifier = Modifier.height(spacing.sm))
        NfctronSubheading("Potrebujem chytrý telefón?")
        NfctronParagraph("Nie. Dobiť náramok je možné na dobíjacom mieste buď kartou alebo hotovosťou.")
    }

    // Nechaj kredit pomáhať
    NfctronSection(heading = "Nechaj svoj kredit pomáhať") {
        NfctronParagraph(
            buildAnnotatedString {
                append("Ak sa rozhodneš nepožiadať o vrátenie zostávajúceho kreditu do ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = White)) { append("14 dní po skončení festivalu") }
                append(", celý nevyplatený zostatok bude v plnej výške venovaný občianskemu združeniu ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = White)) { append("Punkáči deťom") }
                append(".")
            }
        )
        NfctronParagraph("Aj týmto spôsobom môžeš podporiť našu činnosť a pomôcť deťom, ktorým je festival venovaný.")
        Spacer(modifier = Modifier.height(spacing.sm))
        Text(
            text = "Ďakujeme, že pomáhaš spolu s nami.",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = White,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun NfctronSection(heading: String, content: @Composable () -> Unit) {
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
private fun NfctronSubheading(text: String) {
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
private fun NfctronParagraph(text: String) {
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
private fun NfctronParagraph(text: androidx.compose.ui.text.AnnotatedString) {
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
private fun NfctronBullet(text: String) {
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
