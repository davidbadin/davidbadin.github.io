sap.ui.define([],
    function() {
        "use strict";
        
        var sDay1Part1 = new Date("2023", "7", "23", "12", "00");
        var sDay1Part2 = new Date("2023", "7", "24", "00", "00");
        var sDay2Part1 = new Date("2023", "7", "24", "12", "00");
        var sDay2Part2 = new Date("2023", "7", "25", "00", "00");
        var sDay3Part1 = new Date("2023", "7", "25", "12", "00");
        var sDay3Part2 = new Date("2023", "7", "26", "00", "00");
        var sDay4Part1 = new Date("2023", "7", "26", "12", "00");
        var sDay4Part2 = new Date("2023", "7", "27", "00", "00");

        var constants = {

            // Google APIs
            sheetId: "1YuHNmbkh-oGDLgRFZjmwuJaMhWU-iAqsi-MzGUpNS-Q",
            sheetRangeData: "'PD2023'!A2:F100",
			sheetRangeInfo: "'PD2023_info'!A1:B100",
			apiKey: "AIzaSyBIHleeVgn137sWxmlCGvFQjewrv-ueXMI",
            apiUri: "https://sheets.googleapis.com/v4/spreadsheets/",

            // URLS
            spotifyLink: "https://open.spotify.com/playlist/1u76dZPsDmhIZiZlmjbQy2",
            ticketsLink: "https://shop.punkacidetom.sk/",

            // stages
            stage: {
                stageA: "A stage",
                stageB: "B stage"
            },

            //calendar dates (defined above)
            date: {
                day1: {
                    part1: sDay1Part1,
                    part2: sDay1Part2,
                    day: 23,
                    month: 7,   //current month - 1 (january = 0,...)
                },
                day2: {
                    part1: sDay2Part1,
                    part2: sDay2Part2,
                    day: 24,
                    month: 7,
                },
                day3: {
                    part1: sDay3Part1,
                    part2: sDay3Part2,
                    day: 25,
                    month: 7,
                },
                day4: {
                    part1: sDay4Part1,
                    part2: sDay4Part2,
                    day: 26,
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