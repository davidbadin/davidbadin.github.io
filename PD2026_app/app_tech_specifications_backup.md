Technical specification for the app

:::::

App name: pd2026_app
App language: SK (default), EN
Do not translate the festival name, the stage names and data from the sheets (only app texts).

Always use central european formats of date and time (example: 26. 5. 2026, 13:00)

Data source: 
Google Sheets are used as a data source.
https://docs.google.com/spreadsheets/d/e/2PACX-1vSUgp2nzjHK13Xm8EGSq5sTE2ACYwdioXZqPKshVmgWnBS9SbIPBnHMdYBCWfD0dMFV17sBXpF5k3mQ/pubhtml 
Use the method stale-while-revalidate.
Fetching new data: 
- manually in Settings screen
- automatically when starting the app 
- automatically when app is resumed, but only if the data were not updated in the last 30 minutes (to avoid over-usage of data)
Sheet tabs:
PD2026 - data for the most screens
PUSH - data for push notifications

Spotify festival playlist link:
https://open.spotify.com/playlist/5QL8HJ0cWaLGS2Qxby0xDG
Spotify artist link:
https://open.spotify.com/artist/ + value from the sheet (SPOTIFY URL)

:::::

Important keywords: 

Start of the festival
- when the first band(s) start playing, it is the start of the festival. The first band(s) means the band(s) that is/are the first chronologicaly by start date and start time, not the first in the sheet (the sheet might not be sorted)
- determine automatically from the sheet STAGES everytime when fetching new data:

End of the festival
- when the last band(s) end playing, it is the end of the festival. The last band(s) means the band(s) that is/are the last chronologicaly by end date and end time, not the last in the sheet  (the sheet might not be sorted)
- determine automatically from the sheet STAGES everytime when fetching new data:

Festival day
Each festival day starts at 6:00 (morning) and ends at 6:00 (morning) the next day. Example: if a band is playing at 3:00 on Saturday, it still belongs to the Friday festival day. Determine the festival days from the data in the sheet each time the data are updated.

:::::

How the app will look:

Header:
Displayed only when a band is playing currently. Displays, which band(s) is/are playing at the moment, including stage name and small progress bar.
It has to be dynamic - it will appear/disappear when a band starts/ends playing (according the time table).

Footer:
The buttons for the following screens will be there:
- HOME - opens home menu
- TIMETABLE - opens timetable
- SPOTIFY - spotify logo button linked to spotify playlist
- SETTINGS - cog icon which opens settings screen
Create suitable icons (you can use Spotify icon provided in the folder "pd_resources") and add the text below the icon

Main:
Displays one of the screens mentioned below. When opening the app, main will display default screen. The default screen depends on the date and time.
Before festival (before start of the festival): default screen = screen Home (including "countdown" block)
During festival (between start and end of the festival): default screen = screen Timetable
After festival (after the end of the festival): default screen = screen Home (including "thank you" block)

SCREENS (VIEWS):

HOME screen
Home screen contains the following blocks (in one column):
- Logo 
- If it is before festival: "Countdown" block (see below)
- If it is after festival: "thank you" text block (see below)
- Buttons for the following screens: News, Bands, Timeline, Info, Tickets & Eshop
- Spotify player for the festival playlist
- References to Facebook ( https://www.facebook.com/punkacidetom ) and Instagram ( https://www.instagram.com/festival_punkaci_detom/ )

Block "Countdown" - it will display countdown - how long before beginning of the festival (days, hours, minutes and seconds).

Block "Thank you" text: prepare a simple thank you text for the festival participants, say that they were amazing, etc. - 10-20 words - save it to a separate file, so we can update the text later. When generating the android app, load the text for "thank you" block from this file.

TIMETABLE screen
There will be a scrollable timetable to display which bands play which festival day. Use vertical timeline. Do not display the "inactive" hours at the beginning or at the end of the day (e.g. if the first band of the festival day starts at 12:00, there is no need to display timeline from 8:00, the timeline will start at 12:00. The same for the end of the day - display timetable only till the last band of the festival day plays). Different days can have a different timeline start/end. 
There will be buttons to switch the festival days - buttons may change depending on data (example: if another bands and festival days will be added, then a new button has to be added for this new festival day). Use the days of the week for the texts of buttons (like "Friday", not dates). During festival (between the start and end of the festival) the app will automatically display the actual festival day, otherwise it displays day 1 as default. In the first column will be a timeline for the first stage, second stage in the second column,... After clicking on a band, the BAND DETAIL screen will open. 
Each block in the timeline contains:
- the band name 
- favorite icon (heart icon; display only if selected as favorite)
- stage
- start and end time (only times, not dates)
- genre

BANDS screen
List of all bands sorted by SORTING PRIORITY (lowest value of SORTING PRIORITY on top; if priority is initial, the it goes to the bottom) and then by NAME.

BAND DETAIL screen
Details about the band:
- Image (if available - it will have the same name as the band. If not, use just logo). The bottom part of image will fade out and the following data will start in this fading part of the image.
- Name
- Favorite icon (clickable)
- Genre
- Day (real day of the week, not the festival day), start date, start and end time
- Stage
- Description
- Spotify player 

Note: if an user clicks on favorite icon, it will appear also in the timetable (as described above).

NEWS screen
Display news-feed from facebook: https://www.facebook.com/punkacidetom

INFO screen
This will be created in a separate public file (HTML) and app will load it as a webpage inside the app. Link to the file:
https://davidbadin.github.io/PD2026_app/info.html
Use the method stale-while-revalidate for this page. This page will not be huge, will contain some text, pictures and maybe external links. This approach is used so we can edit this page without a need of the app regeneration.

SETTINGS screen
Settings can be done here
- language switch (SK, EN)
- font size (normal, large) - this will adjust the text size and all elements in the app so it is better readable for people with sight handicaps
- update data (fetch the new data from the source, even if the data have been updated less than 15 minutes ago)

TICKETS & ESHOP
The screen displays external link for tickets sale and for Punkáči deťom eshop:
Tickets:
https://punkacidetom.sk/vstupenky/
https://goout.net/sk/punkaci-detom-2026/szbuqay/
Eshop:
https://shop.punkacidetom.sk/

:::

PUSH NOTIFICATIONS
Push notifications need to be implemented. 
Technical settings:
TODO

:::

DESIGN
Backgrounf color: #696969
Main color 1: #0C143B
Main color 2: #AD1C24
Font color: #FFFFFF

Make it look cool, modern, 1979-punk style.

