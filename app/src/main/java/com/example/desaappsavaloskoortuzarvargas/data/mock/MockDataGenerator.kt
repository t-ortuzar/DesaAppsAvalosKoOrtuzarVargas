package com.example.desaappsavaloskoortuzarvargas.data.mock

import com.example.desaappsavaloskoortuzarvargas.domain.model.DLC
import com.example.desaappsavaloskoortuzarvargas.domain.model.DiscountedGame
import com.example.desaappsavaloskoortuzarvargas.domain.model.Game
import com.example.desaappsavaloskoortuzarvargas.domain.model.News
import com.example.desaappsavaloskoortuzarvargas.domain.model.PriceHistory

object MockDataGenerator {

    private data class GameInfo(
        val id: Int,
        val name: String,
        val description: String,
        val tags: List<String> = emptyList(),
        val platforms: List<String> = listOf("Steam", "Epic Games", "GOG", "G2A", "Eneba")
    )

    private val popularGames = listOf(
        GameInfo(1, "Elden Ring", "An action role-playing game developed by FromSoftware and published by Bandai Namco.", listOf("Action", "RPG", "Open World", "Souls-like")),
        GameInfo(2, "Baldur's Gate 3", "A role-playing game based on the Dungeons & Dragons tabletop role-playing system.", listOf("RPG", "Adventure", "Strategy")),
        GameInfo(3, "The Witcher 3: Wild Hunt", "An open world action role-playing game set in a fantasy universe.", listOf("RPG", "Open World", "Action", "Adventure")),
        GameInfo(4, "Cyberpunk 2077", "An open world action-adventure RPG set in the megalopolis of Night City.", listOf("RPG", "FPS", "Open World", "Action")),
        GameInfo(5, "Red Dead Redemption 2", "An epic tale of outlaw life in America's heartland.", listOf("Action", "Adventure", "Open World", "TPS")),
        GameInfo(6, "God of War Ragnarök", "Kratos and Atreus embark on a mythic journey for answers.", listOf("Action", "Adventure"), listOf("Steam", "Epic Games", "G2A", "Eneba")),
        GameInfo(7, "Starfield", "An epic space-faring RPG by Bethesda Game Studios.", listOf("RPG", "Open World", "Action", "Adventure"), listOf("Steam", "G2A", "Eneba")),
        GameInfo(8, "Diablo IV", "A genre-defining action-RPG experience with endless evil to slaughter.", listOf("Action", "RPG", "Co-op"), listOf("Steam", "Battle.net", "G2A", "Eneba")),
        GameInfo(9, "Alan Wake 2", "A survival horror game with a mind-bending story.", listOf("Horror", "Survival", "Action", "Adventure"), listOf("Epic Games", "G2A", "Eneba")),
        GameInfo(10, "Hogwarts Legacy", "An open-world action RPG set in the Wizarding World of Harry Potter.", listOf("RPG", "Open World", "Action", "Adventure")),
        GameInfo(11, "Resident Evil 4 Remake", "A reimagining of the 2005 survival horror classic.", listOf("Horror", "Survival", "Action", "TPS")),
        GameInfo(12, "Death Stranding", "An action game by Kojima Productions connecting isolated cities.", listOf("Action", "Adventure", "Open World")),
        GameInfo(13, "Street Fighter 6", "The next evolution of the legendary fighting game series.", listOf("Fighting", "Action")),
        GameInfo(14, "Tekken 8", "The King of Iron Fist Tournament returns with next-gen visuals.", listOf("Fighting", "Action")),
        GameInfo(15, "Ghost of Tsushima", "An open-world samurai action-adventure game.", listOf("Action", "Adventure", "Open World"), listOf("Steam", "Epic Games", "G2A", "Eneba")),
        GameInfo(16, "The Last of Us Part I", "A remake of the post-apocalyptic action-adventure game.", listOf("Action", "Adventure", "Horror", "Survival"), listOf("Steam", "Epic Games", "G2A", "Eneba")),
        GameInfo(17, "Spider-Man Remastered", "Be the Marvel superhero Spider-Man in a new open-world adventure.", listOf("Action", "Adventure", "Open World"), listOf("Steam", "Epic Games", "G2A", "Eneba")),
        GameInfo(18, "Horizon Zero Dawn", "Explore a vibrant world populated by mysterious machines.", listOf("Action", "RPG", "Open World", "Adventure"), listOf("Steam", "Epic Games", "GOG", "G2A", "Eneba")),
        GameInfo(19, "Dead Space Remake", "A sci-fi survival horror classic rebuilt from the ground up.", listOf("Horror", "Survival", "Action", "TPS")),
        GameInfo(20, "Like a Dragon: Infinite Wealth", "A globe-trotting action-adventure RPG.", listOf("RPG", "Action", "Adventure")),
        GameInfo(21, "Final Fantasy VII Rebirth", "The next chapter in the FFVII remake trilogy.", listOf("RPG", "Action", "Adventure"), listOf("Steam", "Epic Games", "G2A", "Eneba")),
        GameInfo(22, "Persona 5 Royal", "Don the mask and join the Phantom Thieves of Hearts.", listOf("RPG", "Adventure")),
        GameInfo(23, "Metaphor: ReFantazio", "A fantasy RPG from the creators of Persona.", listOf("RPG", "Strategy", "Adventure")),
        GameInfo(24, "Palworld", "A creature-collecting open-world survival crafting game.", listOf("Survival", "Open World", "Co-op", "Action")),
        GameInfo(25, "Black Myth: Wukong", "An action RPG rooted in Chinese mythology.", listOf("Action", "RPG", "Souls-like")),
        GameInfo(26, "S.T.A.L.K.E.R. 2", "A first-person shooter set in the Chornobyl Exclusion Zone.", listOf("FPS", "Horror", "Survival", "Open World")),
        GameInfo(27, "Dragon's Dogma 2", "An open-world action RPG by Capcom.", listOf("RPG", "Action", "Open World", "Adventure")),
        GameInfo(28, "Sekiro: Shadows Die Twice", "A dark and brutal action-adventure from FromSoftware.", listOf("Action", "Souls-like", "Adventure")),
        GameInfo(29, "Indiana Jones and the Great Circle", "An action-adventure in first person.", listOf("Action", "Adventure", "FPS")),
        GameInfo(30, "Senua's Saga: Hellblade II", "An intense action-adventure with stunning visuals.", listOf("Action", "Adventure", "Narrative")),
        GameInfo(31, "Monster Hunter: World", "Hunt massive monsters in a living, breathing ecosystem.", listOf("Action", "RPG", "Co-op")),
        GameInfo(32, "Dark Souls III", "An action RPG set in a dark fantasy world.", listOf("Action", "RPG", "Souls-like")),
        GameInfo(33, "Armored Core VI", "Mech combat action from FromSoftware.", listOf("Action", "Simulation")),
        GameInfo(34, "Jedi: Survivor", "Continue Cal Kestis' journey in this action RPG.", listOf("Action", "Adventure", "RPG"), listOf("Steam", "Epic Games", "EA Play", "G2A", "Eneba")),
        GameInfo(35, "EA Sports FC 25", "The latest football simulation game.", listOf("Sports", "Simulation"), listOf("Steam", "Epic Games", "EA Play", "G2A", "Eneba")),
        GameInfo(36, "Madden NFL 25", "The premier American football simulation.", listOf("Sports", "Simulation"), listOf("Steam", "EA Play", "G2A", "Eneba")),
        GameInfo(37, "NBA 2K25", "The definitive basketball simulation experience.", listOf("Sports", "Simulation")),
        GameInfo(38, "F1 24", "Experience the thrill of Formula 1 racing.", listOf("Racing", "Simulation", "Sports"), listOf("Steam", "Epic Games", "EA Play", "G2A", "Eneba")),
        GameInfo(39, "WWE 2K24", "Step into the squared circle in this wrestling game.", listOf("Sports", "Fighting")),
        GameInfo(40, "The Crew Motorfest", "A festival open-world racing game.", listOf("Racing", "Open World"), listOf("Steam", "Epic Games", "Ubisoft+", "G2A", "Eneba")),
        GameInfo(41, "Need for Speed Unbound", "Street racing with artistic visual style.", listOf("Racing", "Action"), listOf("Steam", "Epic Games", "EA Play", "G2A", "Eneba")),
        GameInfo(42, "Forza Motorsport", "Premium racing simulation with stunning visuals.", listOf("Racing", "Simulation"), listOf("Steam", "G2A", "Eneba")),
        GameInfo(43, "Ghostwire: Tokyo", "Save Tokyo from supernatural threats in first person.", listOf("Action", "FPS", "Horror")),
        GameInfo(44, "Control Ultimate Edition", "A supernatural action-adventure game by Remedy.", listOf("Action", "Adventure", "TPS")),
        GameInfo(45, "The Callisto Protocol", "A survival horror game set on Jupiter's moon.", listOf("Horror", "Survival", "Action")),
        GameInfo(46, "Lies of P", "A Soulslike action RPG inspired by Pinocchio.", listOf("Action", "RPG", "Souls-like")),
        GameInfo(47, "Remnant II", "A co-operative third-person survival shooter.", listOf("Action", "TPS", "Co-op", "Survival")),
        GameInfo(48, "Resident Evil Village", "Survival horror returns with Ethan Winters.", listOf("Horror", "Survival", "FPS")),
        GameInfo(49, "Outlast Trials", "A cooperative psychological horror experience.", listOf("Horror", "Co-op", "Survival")),
        GameInfo(50, "Phasmophobia", "Cooperative ghost hunting with friends.", listOf("Horror", "Co-op", "Simulation")),
        GameInfo(51, "Lethal Company", "A horror game about scavenging on abandoned moons.", listOf("Horror", "Co-op", "Indie")),
        GameInfo(52, "The Forest", "Survive in a forest after a plane crash.", listOf("Survival", "Horror", "Open World")),
        GameInfo(53, "Subnautica", "Explore an alien underwater world.", listOf("Survival", "Adventure", "Open World")),
        GameInfo(54, "No Man's Sky", "Explore an infinite procedurally generated universe.", listOf("Survival", "Adventure", "Open World", "Sandbox")),
        GameInfo(55, "Deep Rock Galactic", "Co-op FPS about space-mining dwarves.", listOf("FPS", "Co-op", "Action")),
        GameInfo(56, "Elite Dangerous", "A space simulation MMO.", listOf("Simulation", "MMO", "Open World")),
        GameInfo(57, "Kerbal Space Program", "Build and fly rockets in a realistic space sim.", listOf("Simulation", "Sandbox")),
        GameInfo(58, "Astroneer", "Explore and reshape distant worlds.", listOf("Sandbox", "Adventure", "Co-op")),
        GameInfo(59, "Satisfactory", "Build massive automated factories.", listOf("Building", "Simulation", "Sandbox", "Co-op")),
        GameInfo(60, "Factorio", "Build and maintain factories on an alien planet.", listOf("Building", "Strategy", "Simulation")),
        GameInfo(61, "Terraria", "Dig, fight, explore, build in a 2D world.", listOf("Sandbox", "Action", "Adventure", "Indie")),
        GameInfo(62, "Valheim", "A brutal Viking survival game.", listOf("Survival", "Co-op", "Open World", "Indie")),
        GameInfo(63, "Grounded", "A survival adventure where you're shrunk to ant size.", listOf("Survival", "Co-op", "Adventure")),
        GameInfo(64, "Rust", "A multiplayer survival game.", listOf("Survival", "FPS", "Open World")),
        GameInfo(65, "DayZ", "Post-apocalyptic open-world survival.", listOf("Survival", "FPS", "Open World")),
        GameInfo(66, "Ark: Survival Evolved", "Survive among dinosaurs on a mysterious island.", listOf("Survival", "Open World", "Co-op")),
        GameInfo(67, "Conan Exiles", "Survive and conquer in the world of Conan the Barbarian.", listOf("Survival", "Open World", "Co-op")),
        GameInfo(68, "Raft", "Survive on a tiny raft in the middle of the ocean.", listOf("Survival", "Co-op", "Adventure")),
        GameInfo(69, "Stray", "Explore a cybercity as a stray cat.", listOf("Adventure", "Indie", "Platformer")),
        GameInfo(70, "Kena: Bridge of Spirits", "A story-driven action-adventure.", listOf("Action", "Adventure", "Indie")),
        GameInfo(71, "It Takes Two", "A crazy fun co-op adventure.", listOf("Co-op", "Adventure", "Platformer")),
        GameInfo(72, "A Way Out", "A cooperative action-adventure game.", listOf("Co-op", "Adventure", "Action")),
        GameInfo(73, "Unravel Two", "A charming puzzle-platformer for two.", listOf("Puzzle", "Platformer", "Co-op", "Indie")),
        GameInfo(74, "Portal 2", "The legendary first-person puzzle game.", listOf("Puzzle", "FPS", "Co-op")),
        GameInfo(75, "The Talos Principle 2", "A philosophical first-person puzzle game.", listOf("Puzzle", "Adventure")),
        GameInfo(76, "Tetris Effect: Connected", "Tetris reimagined with stunning visuals.", listOf("Puzzle", "Indie")),
        GameInfo(77, "Celeste", "A challenging platformer about climbing a mountain.", listOf("Platformer", "Indie")),
        GameInfo(78, "Hollow Knight", "A challenging 2D action-adventure in a vast world.", listOf("Action", "Platformer", "Indie", "Souls-like")),
        GameInfo(79, "Hades", "A rogue-like dungeon crawler from Supergiant Games.", listOf("Roguelike", "Action", "Indie")),
        GameInfo(80, "Dead Cells", "A rogue-lite action-platformer.", listOf("Roguelike", "Action", "Platformer", "Indie")),
        GameInfo(81, "Risk of Rain 2", "A third-person roguelike shooter.", listOf("Roguelike", "TPS", "Co-op")),
        GameInfo(82, "Gunfire Reborn", "A FPS roguelite with RPG elements.", listOf("FPS", "Roguelike", "Co-op", "RPG")),
        GameInfo(83, "Slay the Spire", "A roguelike deckbuilding game.", listOf("Roguelike", "Card Game", "Strategy", "Indie")),
        GameInfo(84, "Monster Train", "A strategic roguelike deckbuilder.", listOf("Roguelike", "Card Game", "Strategy")),
        GameInfo(85, "Inscryption", "An inky card-based odyssey.", listOf("Card Game", "Horror", "Puzzle", "Indie")),
        GameInfo(86, "Dave the Diver", "A casual adventure RPG about deep-sea diving.", listOf("Adventure", "RPG", "Indie")),
        GameInfo(87, "Stardew Valley", "Farm, fish, and build community in a charming RPG.", listOf("RPG", "Simulation", "Indie", "Sandbox")),
        GameInfo(88, "Unpacking", "A zen puzzle game about the experience of moving.", listOf("Puzzle", "Indie", "Narrative")),
        GameInfo(89, "Spiritfarer", "A cozy management game about dying.", listOf("Adventure", "Simulation", "Indie", "Narrative")),
        GameInfo(90, "Outer Wilds", "Explore a solar system stuck in a time loop.", listOf("Adventure", "Puzzle", "Indie", "Open World")),
        GameInfo(91, "What Remains of Edith Finch", "A collection of stories about a cursed family.", listOf("Adventure", "Narrative", "Indie")),
        GameInfo(92, "Firewatch", "A mystery adventure in the Wyoming wilderness.", listOf("Adventure", "Narrative", "Indie")),
        GameInfo(93, "The Stanley Parable: Ultra Deluxe", "A narrative exploration game.", listOf("Adventure", "Narrative", "Indie")),
        GameInfo(94, "The Witness", "An exploration-puzzle game on a mysterious island.", listOf("Puzzle", "Adventure", "Indie")),
        GameInfo(95, "Return of the Obra Dinn", "A mystery puzzle game on a ghost ship.", listOf("Puzzle", "Adventure", "Indie", "Narrative")),
        GameInfo(96, "Disco Elysium: The Final Cut", "A groundbreaking role-playing game.", listOf("RPG", "Narrative", "Adventure")),
        GameInfo(97, "Divinity: Original Sin 2", "A deep tactical RPG with co-op play.", listOf("RPG", "Strategy", "Co-op")),
        GameInfo(98, "Pillars of Eternity II", "An epic RPG adventuring on the high seas.", listOf("RPG", "Strategy", "Adventure")),
        GameInfo(99, "Hitman: World of Assassination", "A stealth assassination sandbox.", listOf("Action", "Adventure", "Open World")),
        GameInfo(100, "Doom Eternal", "A fast-paced first-person shooter.", listOf("FPS", "Action"))
    )

    private val platforms = listOf("Steam", "Epic Games", "GOG", "EA Play", "Ubisoft+", "Battle.net", "G2A", "Eneba")

    private val steamAppIds = mapOf(
        1 to 1245620, 2 to 1086940, 3 to 292030, 4 to 1091500, 5 to 1174180,
        6 to 2322010, 7 to 1716740, 8 to 2344520, 10 to 990080,
        11 to 2050650, 12 to 1190460, 13 to 1364780, 14 to 1778820, 15 to 2215430,
        16 to 1888930, 17 to 1817070, 18 to 1151640, 19 to 1693980, 20 to 2072450,
        21 to 2909400, 22 to 1687950, 23 to 2679460, 24 to 1623730, 25 to 2358720,
        26 to 1643320, 27 to 2054970, 28 to 814380, 29 to 2677660, 30 to 2461850,
        31 to 582010, 32 to 374320, 33 to 1888160, 34 to 1774580, 35 to 2669320,
        36 to 2582560, 37 to 2878980, 38 to 2488620, 39 to 2315690, 40 to 2698940,
        41 to 1846380, 42 to 2440510, 43 to 1475810, 44 to 870780, 45 to 1544020,
        46 to 1627720, 47 to 1282100, 48 to 1196590, 49 to 1304930, 50 to 739630,
        51 to 1966720, 52 to 242760, 53 to 264710, 54 to 275850, 55 to 548430,
        56 to 359320, 57 to 220200, 58 to 361420, 59 to 526870, 60 to 427520,
        61 to 105600, 62 to 892970, 63 to 962130, 64 to 252490, 65 to 221100,
        66 to 346110, 67 to 440900, 68 to 648800, 69 to 1332010, 70 to 1276340,
        71 to 1426210, 72 to 1222700, 73 to 1225570, 74 to 620, 75 to 835960,
        76 to 1003590, 77 to 504230, 78 to 367520, 79 to 1145360, 80 to 588650,
        81 to 632360, 82 to 1217060, 83 to 646570, 84 to 1102190, 85 to 1092790,
        86 to 1868140, 87 to 413150, 88 to 1135690, 89 to 972660, 90 to 753640,
        91 to 501300, 92 to 383870, 93 to 1703340, 94 to 210970, 95 to 653530,
        96 to 632470, 97 to 435150, 98 to 560130, 99 to 1659040, 100 to 782330
    )

    // Games NOT on Steam - use alternative image sources
    private val alternativeImages = mapOf(
        9 to "https://gaming-cdn.com/images/products/14178/616x353/alan-wake-2-pc-game-epic-games-cover.jpg"
    )

    private val freeGameSteamIds = mapOf(
        "Warframe" to 230410, "Path of Exile" to 238960, "Lost Ark" to 1599340,
        "Albion Online" to 761890, "Old School RuneScape" to 1343370,
        "Black Desert Online" to 582660, "Neverwinter" to 109600,
        "Star Wars: The Old Republic" to 1286830, "Guild Wars 2" to 1284210,
        "Dauntless" to 1269610, "Apex Legends" to 1172470, "Valorant" to 2215240,
        "Counter-Strike 2" to 730, "Halo Infinite Multiplayer" to 1240440,
        "Destiny 2: New Light" to 1085660, "Genshin Impact" to 2777620,
        "Honkai: Star Rail" to 2346820, "World of Tanks" to 1407200,
        "War Thunder" to 236390, "Brawlhalla" to 291550,
        "Rocket League" to 252950, "Fall Guys" to 1097150, "MultiVersus" to 1818750
    )

    // Temporarily free games (limited time promotions, not permanently F2P)
    private data class TempFreeGame(val name: String, val steamId: Int, val platform: String, val endDate: String)
    private val temporarilyFreeGames = listOf(
        TempFreeGame("Tomb Raider (2013)", 203160, "Epic Games", "2026-05-18"),
        TempFreeGame("Stellaris", 281990, "Epic Games", "2026-05-20"),
        TempFreeGame("For Honor", 304390, "Ubisoft+", "2026-05-22"),
        TempFreeGame("Cities: Skylines", 255710, "Steam", "2026-05-15"),
        TempFreeGame("Batman: Arkham Knight", 208650, "Epic Games", "2026-05-25"),
        TempFreeGame("Metro 2033 Redux", 286690, "Steam", "2026-05-17"),
        TempFreeGame("Prey (2017)", 480490, "Epic Games", "2026-05-19"),
        TempFreeGame("Amnesia: The Dark Descent", 57300, "GOG", "2026-05-16"),
        TempFreeGame("Inside", 304430, "Epic Games", "2026-05-21"),
        TempFreeGame("Limbo", 48000, "Steam", "2026-05-23"),
        TempFreeGame("Sable", 757310, "Epic Games", "2026-05-24"),
        TempFreeGame("A Plague Tale: Innocence", 752590, "Epic Games", "2026-05-18"),
        TempFreeGame("Hyper Light Drifter", 257850, "Epic Games", "2026-05-20"),
        TempFreeGame("Overcooked! 2", 728880, "Epic Games", "2026-05-22"),
        TempFreeGame("Abzu", 384190, "Epic Games", "2026-05-25"),
        TempFreeGame("Murderous Pursuits", 638070, "Steam", "2026-05-16"),
        TempFreeGame("Total War: Shogun 2", 201270, "Steam", "2026-05-19"),
        TempFreeGame("World War Z", 699130, "Epic Games", "2026-05-21"),
        TempFreeGame("Evoland", 233470, "GOG", "2026-05-23"),
        TempFreeGame("Drawful 2", 442070, "Steam", "2026-05-24")
    )

    // DLC data for some popular games
    private data class DLCInfo(val id: Int, val name: String, val gameId: Int, val desc: String)
    private val dlcData = listOf(
        DLCInfo(1001, "Shadow of the Erdtree", 1, "A massive DLC expansion for Elden Ring."),
        DLCInfo(1002, "Phantom Liberty", 4, "A spy-thriller expansion for Cyberpunk 2077."),
        DLCInfo(1003, "Blood and Wine", 3, "A standalone-quality expansion for The Witcher 3."),
        DLCInfo(1004, "Hearts of Stone", 3, "A story-driven expansion for The Witcher 3."),
        DLCInfo(1005, "The Frozen Wilds", 18, "An icy expansion for Horizon Zero Dawn."),
        DLCInfo(1006, "Vessel of Hatred", 8, "The first expansion for Diablo IV."),
        DLCInfo(1007, "Burning Shores", 18, "A DLC for Horizon set in Los Angeles ruins."),
        DLCInfo(1008, "Sunbreak", 31, "A massive expansion for Monster Hunter Rise."),
        DLCInfo(1009, "Iceborne", 31, "The master expansion for Monster Hunter World."),
        DLCInfo(1010, "The Ringed City", 32, "The final DLC for Dark Souls III."),
        DLCInfo(1011, "Ashes of Ariandel", 32, "A DLC expansion for Dark Souls III."),
        DLCInfo(1012, "Night of the Raven", 9, "An expansion to Alan Wake 2."),
        DLCInfo(1013, "Echoes of the Eye", 90, "A hauntingly beautiful expansion for Outer Wilds."),
        DLCInfo(1014, "The Foundation", 44, "The first expansion for Control."),
        DLCInfo(1015, "AWE", 44, "The second expansion for Control, connecting to Alan Wake."),
        DLCInfo(1016, "Tiny Tina's Assault", 97, "A DLC adventure for Divinity: Original Sin 2."),
        DLCInfo(1017, "Seven Deadly Sins", 99, "A DLC pack for Hitman."),
        DLCInfo(1018, "The Ancient Gods Part 1", 100, "Campaign DLC for Doom Eternal."),
        DLCInfo(1019, "The Ancient Gods Part 2", 100, "Campaign DLC for Doom Eternal."),
        DLCInfo(1020, "Winters' Expansion", 48, "Expansion for Resident Evil Village.")
    )

    // Steam DLC App IDs (some real, some approximate)
    private val dlcSteamIds = mapOf(
        1001 to 2778580, 1002 to 1905880, 1003 to 378648, 1004 to 378649,
        1005 to 1151641, 1006 to 2344521, 1007 to 2215431, 1008 to 1880360,
        1009 to 1118010, 1010 to 506970, 1011 to 506971, 1012 to 558551,
        1013 to 753641, 1014 to 870781, 1015 to 870782, 1016 to 435151,
        1017 to 1659041, 1018 to 1098291, 1019 to 1098292, 1020 to 1196591
    )

    private fun steamImg(gameId: Int): String {
        // Check if game has an alternative image (not on Steam)
        alternativeImages[gameId]?.let { return it }
        val appId = steamAppIds[gameId] ?: return "https://cdn.akamai.steamstatic.com/steam/apps/1245620/header.jpg"
        return "https://cdn.akamai.steamstatic.com/steam/apps/$appId/header.jpg"
    }

    private fun freeGameImg(name: String): String {
        val appId = freeGameSteamIds[name] ?: return "https://cdn.akamai.steamstatic.com/steam/apps/230410/header.jpg"
        return "https://cdn.akamai.steamstatic.com/steam/apps/$appId/header.jpg"
    }

    private fun dlcImg(dlcId: Int): String {
        val appId = dlcSteamIds[dlcId] ?: return "https://cdn.akamai.steamstatic.com/steam/apps/1245620/header.jpg"
        return "https://cdn.akamai.steamstatic.com/steam/apps/$appId/header.jpg"
    }

    private fun generateDLCsForGame(gameId: Int): List<DLC> {
        return dlcData.filter { it.gameId == gameId }.map { dlcInfo ->
            val basePrices = mapOf(
                "Steam" to (14.99f + (dlcInfo.id % 10) * 0.5f),
                "Epic Games" to (13.99f + (dlcInfo.id % 10) * 0.5f),
                "GOG" to (12.99f + (dlcInfo.id % 10) * 0.5f),
                "G2A" to (9.99f + (dlcInfo.id % 10) * 0.3f),
                "Eneba" to (8.99f + (dlcInfo.id % 10) * 0.3f)
            )
            DLC(
                id = dlcInfo.id,
                name = dlcInfo.name,
                gameId = gameId,
                imageUrl = dlcImg(dlcInfo.id),
                currentPrices = basePrices,
                historicalDiscount = 20 + (dlcInfo.id % 50),
                releaseDate = "2024-${String.format("%02d", (dlcInfo.id % 12) + 1)}-15",
                description = dlcInfo.desc
            )
        }
    }

    fun generateGames(): List<Game> = popularGames.map { info ->
        val allPrices = mapOf(
            "Steam" to (29.99f + (info.id % 40) * 0.5f),
            "Epic Games" to (29.99f + (info.id % 40) * 0.5f - 2f).coerceAtLeast(9.99f),
            "GOG" to (27.99f + (info.id % 40) * 0.5f),
            "EA Play" to (24.99f + (info.id % 30) * 0.4f),
            "Ubisoft+" to (24.99f + (info.id % 30) * 0.4f),
            "Battle.net" to (29.99f + (info.id % 35) * 0.3f),
            "G2A" to (19.99f + (info.id % 30) * 0.4f),
            "Eneba" to (17.99f + (info.id % 25) * 0.4f)
        )
        // Only include prices for platforms where the game is actually available
        val filteredPrices = allPrices.filter { it.key in info.platforms }
        Game(
            id = info.id,
            name = info.name,
            description = info.description,
            releaseDate = "2024-${String.format("%02d", (info.id % 12) + 1)}-${String.format("%02d", (info.id % 28) + 1)}",
            imageUrl = steamImg(info.id),
            rating = 7.0 + (info.id % 30) * 0.1,
            currentPrices = filteredPrices,
            historicalDiscount = 10 + (info.id % 80),
            tags = info.tags,
            dlcs = generateDLCsForGame(info.id),
            availablePlatforms = info.platforms
        )
    }

    fun generateDiscounts(): List<DiscountedGame> {
        val discounts = mutableListOf<DiscountedGame>()

        // 50 discounted paid games
        for (i in 0 until 50) {
            val game = popularGames[i % popularGames.size]
            val platform = platforms[i % platforms.size]
            val originalPrice = 29.99f + (game.id % 40) * 0.5f
            val discountPct = 15 + (i * 37 % 76)
            val currentPrice = originalPrice * (1f - discountPct / 100f)
            discounts.add(
                DiscountedGame(
                    gameId = game.id,
                    gameName = game.name,
                    imageUrl = steamImg(game.id),
                    platform = platform,
                    originalPrice = originalPrice,
                    currentPrice = currentPrice,
                    discountPercentage = discountPct,
                    isFree = false,
                    isF2P = false,
                    isTemporarilyFree = false,
                    endDate = "2026-06-${String.format("%02d", (i % 28) + 1)}",
                    isHistoricalLowest = discountPct >= 70,
                    tags = game.tags
                )
            )
        }

        // F2P games (permanently free)
        val f2pNames = listOf(
            "Warframe", "Path of Exile", "Lost Ark", "Albion Online",
            "Old School RuneScape", "Black Desert Online", "Neverwinter",
            "Star Wars: The Old Republic", "Guild Wars 2", "Dauntless",
            "Apex Legends", "Valorant", "Counter-Strike 2", "Halo Infinite Multiplayer",
            "Destiny 2: New Light", "Genshin Impact", "Honkai: Star Rail",
            "World of Tanks", "War Thunder", "Brawlhalla",
            "Rocket League", "Fall Guys", "MultiVersus"
        )
        val f2pTags = mapOf(
            "Warframe" to listOf("Free2Play", "TPS", "Co-op", "Action"),
            "Path of Exile" to listOf("Free2Play", "RPG", "Action"),
            "Lost Ark" to listOf("Free2Play", "RPG", "MMO", "Action"),
            "Albion Online" to listOf("Free2Play", "MMO", "Sandbox"),
            "Old School RuneScape" to listOf("Free2Play", "RPG", "MMO"),
            "Black Desert Online" to listOf("Free2Play", "MMO", "RPG", "Action"),
            "Neverwinter" to listOf("Free2Play", "RPG", "MMO"),
            "Star Wars: The Old Republic" to listOf("Free2Play", "RPG", "MMO"),
            "Guild Wars 2" to listOf("Free2Play", "RPG", "MMO"),
            "Dauntless" to listOf("Free2Play", "Action", "Co-op"),
            "Apex Legends" to listOf("Free2Play", "FPS", "Battle Royale"),
            "Valorant" to listOf("Free2Play", "FPS"),
            "Counter-Strike 2" to listOf("Free2Play", "FPS"),
            "Halo Infinite Multiplayer" to listOf("Free2Play", "FPS"),
            "Destiny 2: New Light" to listOf("Free2Play", "FPS", "Co-op", "MMO"),
            "Genshin Impact" to listOf("Free2Play", "RPG", "Open World", "Action"),
            "Honkai: Star Rail" to listOf("Free2Play", "RPG"),
            "World of Tanks" to listOf("Free2Play", "Action", "Simulation"),
            "War Thunder" to listOf("Free2Play", "Action", "Simulation"),
            "Brawlhalla" to listOf("Free2Play", "Fighting"),
            "Rocket League" to listOf("Free2Play", "Sports", "Racing"),
            "Fall Guys" to listOf("Free2Play", "Platformer"),
            "MultiVersus" to listOf("Free2Play", "Fighting")
        )
        f2pNames.forEachIndexed { idx, name ->
            discounts.add(
                DiscountedGame(
                    gameId = 1000 + idx,
                    gameName = name,
                    imageUrl = freeGameImg(name),
                    platform = platforms[idx % platforms.size],
                    originalPrice = 0f,
                    currentPrice = 0f,
                    discountPercentage = 100,
                    isFree = true,
                    isF2P = true,
                    isTemporarilyFree = false,
                    endDate = null,
                    isHistoricalLowest = false,
                    tags = f2pTags[name] ?: listOf("Free2Play")
                )
            )
        }

        // Temporarily free games (limited-time promotions)
        temporarilyFreeGames.forEachIndexed { idx, game ->
            discounts.add(
                DiscountedGame(
                    gameId = 2000 + idx,
                    gameName = game.name,
                    imageUrl = "https://cdn.akamai.steamstatic.com/steam/apps/${game.steamId}/header.jpg",
                    platform = game.platform,
                    originalPrice = 19.99f + (idx * 5f),
                    currentPrice = 0f,
                    discountPercentage = 100,
                    isFree = true,
                    isF2P = false,
                    isTemporarilyFree = true,
                    endDate = game.endDate,
                    isHistoricalLowest = true,
                    tags = listOf("Action", "Adventure")
                )
            )
        }

        return discounts
    }

    fun generateNews(): List<News> {
        val newsList = mutableListOf<News>()
        val headlines = listOf(
            "Price dropped to historical low!" to "discount",
            "New DLC expansion announced" to "update",
            "Major patch update released" to "update",
            "Weekend sale event" to "discount",
            "Limited-time bundle deal" to "discount",
            "Community event starting soon" to "event",
            "Next-gen upgrade available" to "update",
            "Free weekend trial" to "discount"
        )

        for (i in 0 until 60) {
            val game = popularGames[i % popularGames.size]
            val (headline, category) = headlines[i % headlines.size]
            val platform = platforms[i % platforms.size]
            newsList.add(
                News(
                    id = i + 1,
                    title = "${game.name} — $headline",
                    content = "Great news for ${game.name} fans! $headline Check out the latest information on $platform. This is a developing story with more details coming soon. Players have been eagerly waiting for this announcement.",
                    imageUrl = steamImg(game.id),
                    date = "2026-${String.format("%02d", (i % 5) + 1)}-${String.format("%02d", (i % 28) + 1)}",
                    gameId = game.id,
                    platform = platform,
                    category = category
                )
            )
        }
        return newsList
    }

    fun generatePriceHistory(): List<PriceHistory> {
        val history = mutableListOf<PriceHistory>()
        for (gameIdx in 0 until 20) {
            val game = popularGames[gameIdx]
            val basePrice = 29.99f + (game.id % 40) * 0.5f
            for (month in 1..12) {
                for (platform in platforms) {
                    val discountPct = ((month * 7 + platforms.indexOf(platform) * 11) % 80)
                    val price = basePrice * (1f - discountPct / 100f)
                    history.add(
                        PriceHistory(
                            gameId = game.id,
                            platform = platform,
                            price = price,
                            discount = discountPct,
                            date = "2025-${String.format("%02d", month)}-15",
                            isHistoricalLowest = discountPct >= 70
                        )
                    )
                }
            }
        }
        return history
    }
}
