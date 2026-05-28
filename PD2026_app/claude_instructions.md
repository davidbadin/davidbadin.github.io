I want to create an android app for a music festival with Claude Code. 
Technical specifications for the app: app_tech_specifications.md
My hardware/software specifications: hardware-software-setup.md
Guide for other setup/tasks: guide.md

Do the followings:
1. Update/rewrite the technical specifications for the app (app_tech_specifications.md): add other necessary specifications, rewrite to make it more suitable for Claude Code, insert placeholders if any information need to be added manually. Summarize the main changes. Skip TODOs, it will be added manually later.
2. Create a prompt for Claude Code, which will generate the app. The files mentioned above will be provided to Claude Code too, so you don't have to copy information from there. Create new file claude_code_prompt.md and add this prompt to this new file.
3. Update file guide.md with the relevant instructions for me (described below).
4. Let me know if you can read and use this data source in this format (Google Sheets - it should be available for public): https://docs.google.com/spreadsheets/d/e/2PACX-1vSUgp2nzjHK13Xm8EGSq5sTE2ACYwdioXZqPKshVmgWnBS9SbIPBnHMdYBCWfD0dMFV17sBXpF5k3mQ/pubhtml If not, add instructions to guide.md how to make this sheet readable for you.
5. Create a simple file in PD2026_app (info.html). Put a placeholder text there ("INFO" and something like - here will be information about the festival). This file will be available online (from https://davidbadin.github.io/PD2026_app/info.html ) and will be loaded inside the app.

Information about this project:

This will be an official app for a punk festival. 
Festival name - general: "Punkáči deťom"
Festival name - year specific: "Punkáči deťom 15" (but sometimes mentioned as "Punkáči deťom 2026")
This app will provide necessary informations for the visitors, link to tickets or merchendise, news,... Important part of the app is an interactive time table, which will display which band is playing what time. User can scroll through the time table, click on a band to see details, or mark bands which he would like to see. It should also be able to play music via Spotify (see the snippet below).

Other information you might need to provide to Claude Code:

STAGES:
1: Punk For Children Stage
2: United Stage
(the stage names can change later, keep it mind. Do not translate them to other langauges!)

Design - it is a punk festival, so the app has to look good to punkers. CSS should be used for the most of design attributes so it is easy to change design directly in CSS files.

Claude Code has to prepare also .gitignore file.

Propose also solution for translations - it might happen that some things will not be translated to slovak or english language correctly and I will need them to correct manually. Some i18n files editable in a text editor (not in Android Studio) would be great. When Android Studio will generate the app, it will get the translations from these files. Add instructions for translations to guide.md.

Sections from the main screen (Bands, Timeline, Info, Tickets & Eshop,..) need to be done in Android Studio in modular way - in case of need we can just remove a section (or add a new one) without affecting functionality of the app or the other screens/modules. This will help us to maintain the app in the future.

The app will use push notifications. Flow: a short text is entered to Google Form → Google Sheets → Apps Script Macro → Firebase FCM → Android Device displays the short text. I have no experiences how to set up this, I will need a guide for Firebase setup and a source code for Apps Script. Add it to guide.md.

guide.md: Add other important instructions that need to be done before/after Claude Code generates the app. Assume that both PCs are setup properly for the development, no need to mention that. Mention publishing the app to Google Store, but no need to create detailed instructions yet.

Ask me if you need to know any androis specs (SDK,...).

Here is an example of sourcecode for Spotify player:
<iframe data-testid="embed-iframe" style="border-radius:12px" src="https://open.spotify.com/embed/playlist/5QL8HJ0cWaLGS2Qxby0xDG?utm_source=generator&theme=0" width="100%" height="352" frameBorder="0" allowfullscreen="" allow="autoplay; clipboard-write; encrypted-media; fullscreen; picture-in-picture" loading="lazy"></iframe>
Let Claude Code use this (or something similat); instruct him to adjust parameters as needed (mainly src, width, height,...).

News screen will show a newsfeed from facebook - https://www.facebook.com/punkacidetom - do you need extra information about this? Do we need any tokens, login, API keays or antything for this? If yes, add it to guide.md .

The app will have a normal size font and large size font (switchable in setting menu).
