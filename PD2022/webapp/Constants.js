sap.ui.define([],
    function() {
        "use strict";
        
        var sDay1Part1 = new Date("2022", "7", "24", "12", "00");
        var sDay1Part2 = new Date("2022", "7", "25", "00", "00");
        var sDay2Part1 = new Date("2022", "7", "25", "12", "00");
        var sDay2Part2 = new Date("2022", "7", "26", "00", "00");
        var sDay3Part1 = new Date("2022", "7", "26", "12", "00");
        var sDay3Part2 = new Date("2022", "7", "27", "00", "00");
        var sDay4Part1 = new Date("2022", "7", "27", "12", "00");
        var sDay4Part2 = new Date("2022", "7", "28", "00", "00");

        var constants = {

            // Google APIs
            sheetId: "1O9khRTDa9-hkyBLW_ZNEKgHbaaWqK5uZ0GYbkr1G0o8",
            sheetRangeData: "'PD2022'!A2:F100",
			sheetRangeInfo: "'PD2022_info'!A1:B100",
			apiKey: "AIzaSyBIHleeVgn137sWxmlCGvFQjewrv-ueXMI",
            apiUri: "https://sheets.googleapis.com/v4/spreadsheets/",

            // URLS
            spotifyLink: "https://open.spotify.com/playlist/2AfkiP1vIJoeYqGkceDf39",
            ticketsLink: "https://www.punkacidetom.sk/listky",

            // stages
            stage: {
                stageA: "Hlavný stage",
                stageB: "B scéna"
            },

            //calendar dates (defined above)
            date: {
                day1: {
                    part1: sDay1Part1,
                    part2: sDay1Part2,
                    day: 24,
                    month: 7,   //current month - 1 (january = 0,...)
                },
                day2: {
                    part1: sDay2Part1,
                    part2: sDay2Part2,
                    day: 25,
                    month: 7,
                },
                day3: {
                    part1: sDay3Part1,
                    part2: sDay3Part2,
                    day: 26,
                    month: 7,
                },
                day4: {
                    part1: sDay4Part1,
                    part2: sDay4Part2,
                    day: 27,
                    month: 7,
                },
            },

            //icons
            iconFavorOn: "sap-icon://heart",
            iconFavorOff: "sap-icon://heart-2",
            iconSpotify: "sap-icon://media-play",
            iconClose: "sap-icon://decline"
        };
    
        return constants;
    });