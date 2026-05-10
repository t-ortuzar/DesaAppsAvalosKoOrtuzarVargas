package com.example.desaappsavaloskoortuzarvargas.data.mock

import com.example.desaappsavaloskoortuzarvargas.domain.model.DiscountedGame
import com.example.desaappsavaloskoortuzarvargas.domain.model.Game
import com.example.desaappsavaloskoortuzarvargas.domain.model.News
import com.example.desaappsavaloskoortuzarvargas.domain.model.PriceHistory

object MockDataGenerator {

    private data class GameInfo(val id: Int, val name: String, val description: String)

    private val popularGames = listOf(
        GameInfo(1, "Elden Ring", "An action role-playing game developed by FromSoftware and published by Bandai Namco."),
        GameInfo(2, "Baldur's Gate 3", "A role-playing game based on the Dungeons & Dragons tabletop role-playing system."),
        GameInfo(3, "The Witcher 3: Wild Hunt", "An open world action role-playing game set in a fantasy universe."),
        GameInfo(4, "Cyberpunk 2077", "An open world action-adventure RPG set in the megalopolis of Night City."),
        GameInfo(5, "Red Dead Redemption 2", "An epic tale of outlaw life in America's heartland."),
        GameInfo(6, "God of War Ragnarök", "Kratos and Atreus embark on a mythic journey for answers."),
        GameInfo(7, "Starfield", "An epic space-faring RPG by Bethesda Game Studios."),
        GameInfo(8, "Diablo IV", "A genre-defining action-RPG experience with endless evil to slaughter."),
        GameInfo(9, "Alan Wake 2", "A survival horror game with a mind-bending story."),
        GameInfo(10, "Hogwarts Legacy", "An open-world action RPG set in the Wizarding World of Harry Potter."),
        GameInfo(11, "Resident Evil 4 Remake", "A reimagining of the 2005 survival horror classic."),
        GameInfo(12, "Death Stranding", "An action game by Kojima Productions connecting isolated cities."),
        GameInfo(13, "Street Fighter 6", "The next evolution of the legendary fighting game series."),
        GameInfo(14, "Tekken 8", "The King of Iron Fist Tournament returns with next-gen visuals."),
        GameInfo(15, "Ghost of Tsushima", "An open-world samurai action-adventure game."),
        GameInfo(16, "The Last of Us Part I", "A remake of the post-apocalyptic action-adventure game."),
        GameInfo(17, "Spider-Man Remastered", "Be the Marvel superhero Spider-Man in a new open-world adventure."),
        GameInfo(18, "Horizon Zero Dawn", "Explore a vibrant world populated by mysterious machines."),
        GameInfo(19, "Dead Space Remake", "A sci-fi survival horror classic rebuilt from the ground up."),
        GameInfo(20, "Like a Dragon: Infinite Wealth", "A globe-trotting action-adventure RPG."),
        GameInfo(21, "Final Fantasy VII Rebirth", "The next chapter in the FFVII remake trilogy."),
        GameInfo(22, "Persona 5 Royal", "Don the mask and join the Phantom Thieves of Hearts."),
        GameInfo(23, "Metaphor: ReFantazio", "A fantasy RPG from the creators of Persona."),
        GameInfo(24, "Palworld", "A creature-collecting open-world survival crafting game."),
        GameInfo(25, "Black Myth: Wukong", "An action RPG rooted in Chinese mythology."),
        GameInfo(26, "S.T.A.L.K.E.R. 2", "A first-person shooter set in the Chornobyl Exclusion Zone."),
        GameInfo(27, "Dragon's Dogma 2", "An open-world action RPG by Capcom."),
        GameInfo(28, "Sekiro: Shadows Die Twice", "A dark and brutal action-adventure from FromSoftware."),
        GameInfo(29, "Indiana Jones and the Great Circle", "An action-adventure in first person."),
        GameInfo(30, "Senua's Saga: Hellblade II", "An intense action-adventure with stunning visuals."),
        GameInfo(31, "Monster Hunter: World", "Hunt massive monsters in a living, breathing ecosystem."),
        GameInfo(32, "Dark Souls III", "An action RPG set in a dark fantasy world."),
        GameInfo(33, "Armored Core VI", "Mech combat action from FromSoftware."),
        GameInfo(34, "Jedi: Survivor", "Continue Cal Kestis' journey in this action RPG."),
        GameInfo(35, "EA Sports FC 25", "The latest football simulation game."),
        GameInfo(36, "Madden NFL 25", "The premier American football simulation."),
        GameInfo(37, "NBA 2K25", "The definitive basketball simulation experience."),
        GameInfo(38, "F1 24", "Experience the thrill of Formula 1 racing."),
        GameInfo(39, "WWE 2K24", "Step into the squared circle in this wrestling game."),
        GameInfo(40, "The Crew Motorfest", "A festival open-world racing game."),
        GameInfo(41, "Need for Speed Unbound", "Street racing with artistic visual style."),
        GameInfo(42, "Forza Motorsport", "Premium racing simulation with stunning visuals."),
        GameInfo(43, "Ghostwire: Tokyo", "Save Tokyo from supernatural threats in first person."),
        GameInfo(44, "Control Ultimate Edition", "A supernatural action-adventure game by Remedy."),
        GameInfo(45, "The Callisto Protocol", "A survival horror game set on Jupiter's moon."),
        GameInfo(46, "Lies of P", "A Soulslike action RPG inspired by Pinocchio."),
        GameInfo(47, "Remnant II", "A co-operative third-person survival shooter."),
        GameInfo(48, "Resident Evil Village", "Survival horror returns with Ethan Winters."),
        GameInfo(49, "Outlast Trials", "A cooperative psychological horror experience."),
        GameInfo(50, "Phasmophobia", "Cooperative ghost hunting with friends."),
        GameInfo(51, "Lethal Company", "A horror game about scavenging on abandoned moons."),
        GameInfo(52, "The Forest", "Survive in a forest after a plane crash."),
        GameInfo(53, "Subnautica", "Explore an alien underwater world."),
        GameInfo(54, "No Man's Sky", "Explore an infinite procedurally generated universe."),
        GameInfo(55, "Deep Rock Galactic", "Co-op FPS about space-mining dwarves."),
        GameInfo(56, "Elite Dangerous", "A space simulation MMO."),
        GameInfo(57, "Kerbal Space Program", "Build and fly rockets in a realistic space sim."),
        GameInfo(58, "Astroneer", "Explore and reshape distant worlds."),
        GameInfo(59, "Satisfactory", "Build massive automated factories."),
        GameInfo(60, "Factorio", "Build and maintain factories on an alien planet."),
        GameInfo(61, "Terraria", "Dig, fight, explore, build in a 2D world."),
        GameInfo(62, "Valheim", "A brutal Viking survival game."),
        GameInfo(63, "Grounded", "A survival adventure where you're shrunk to ant size."),
        GameInfo(64, "Rust", "A multiplayer survival game."),
        GameInfo(65, "DayZ", "Post-apocalyptic open-world survival."),
        GameInfo(66, "Ark: Survival Evolved", "Survive among dinosaurs on a mysterious island."),
        GameInfo(67, "Conan Exiles", "Survive and conquer in the world of Conan the Barbarian."),
        GameInfo(68, "Raft", "Survive on a tiny raft in the middle of the ocean."),
        GameInfo(69, "Stray", "Explore a cybercity as a stray cat."),
        GameInfo(70, "Kena: Bridge of Spirits", "A story-driven action-adventure."),
        GameInfo(71, "It Takes Two", "A crazy fun co-op adventure."),
        GameInfo(72, "A Way Out", "A cooperative action-adventure game."),
        GameInfo(73, "Unravel Two", "A charming puzzle-platformer for two."),
        GameInfo(74, "Portal 2", "The legendary first-person puzzle game."),
        GameInfo(75, "The Talos Principle 2", "A philosophical first-person puzzle game."),
        GameInfo(76, "Tetris Effect: Connected", "Tetris reimagined with stunning visuals."),
        GameInfo(77, "Celeste", "A challenging platformer about climbing a mountain."),
        GameInfo(78, "Hollow Knight", "A challenging 2D action-adventure in a vast world."),
        GameInfo(79, "Hades", "A rogue-like dungeon crawler from Supergiant Games."),
        GameInfo(80, "Dead Cells", "A rogue-lite action-platformer."),
        GameInfo(81, "Risk of Rain 2", "A third-person roguelike shooter."),
        GameInfo(82, "Gunfire Reborn", "A FPS roguelite with RPG elements."),
        GameInfo(83, "Slay the Spire", "A roguelike deckbuilding game."),
        GameInfo(84, "Monster Train", "A strategic roguelike deckbuilder."),
        GameInfo(85, "Inscryption", "An inky card-based odyssey."),
        GameInfo(86, "Dave the Diver", "A casual adventure RPG about deep-sea diving."),
        GameInfo(87, "Stardew Valley", "Farm, fish, and build community in a charming RPG."),
        GameInfo(88, "Unpacking", "A zen puzzle game about the experience of moving."),
        GameInfo(89, "Spiritfarer", "A cozy management game about dying."),
        GameInfo(90, "Outer Wilds", "Explore a solar system stuck in a time loop."),
        GameInfo(91, "What Remains of Edith Finch", "A collection of stories about a cursed family."),
        GameInfo(92, "Firewatch", "A mystery adventure in the Wyoming wilderness."),
        GameInfo(93, "The Stanley Parable: Ultra Deluxe", "A narrative exploration game."),
        GameInfo(94, "The Witness", "An exploration-puzzle game on a mysterious island."),
        GameInfo(95, "Return of the Obra Dinn", "A mystery puzzle game on a ghost ship."),
        GameInfo(96, "Disco Elysium: The Final Cut", "A groundbreaking role-playing game."),
        GameInfo(97, "Divinity: Original Sin 2", "A deep tactical RPG with co-op play."),
        GameInfo(98, "Pillars of Eternity II", "An epic RPG adventuring on the high seas."),
        GameInfo(99, "Hitman: World of Assassination", "A stealth assassination sandbox."),
        GameInfo(100, "Doom Eternal", "A fast-paced first-person shooter.")
    )

    private val platforms = listOf("Steam", "Epic Games", "GOG", "EA Play", "Ubisoft+", "Battle.net", "G2A", "Eneba")

    // Real Steam App IDs for header images
    private val steamAppIds = mapOf(
        1 to 1245620, 2 to 1086940, 3 to 292030, 4 to 1091500, 5 to 1174180,
        6 to 2322010, 7 to 1716740, 8 to 2344520, 9 to 558550, 10 to 990080,
        11 to 2050650, 12 to 1190460, 13 to 1364780, 14 to 1778820, 15 to 2215430,
        16 to 1888930, 17 to 1817070, 18 to 1151640, 19 to 1693980, 20 to 2072450,
        21 to 2909400, 22 to 1687950, 23 to 2679460, 24 to 1623730, 25 to 2358720,
        26 to 1643320, 27 to 2054970, 28 to 814380, 29 to 2677660, 30 to 2740960,
        31 to 582010, 32 to 374320, 33 to 1888160, 34 to 1774580, 35 to 2669320,
        36 to 2460080, 37 to 2338770, 38 to 2488620, 39 to 2492810, 40 to 2091430,
        41 to 1846380, 42 to 2440510, 43 to 1056960, 44 to 870780, 45 to 1544020,
        46 to 1627720, 47 to 1282100, 48 to 1196590, 49 to 1304930, 50 to 739630,
        51 to 1966720, 52 to 242760, 53 to 264710, 54 to 275850, 55 to 548430,
        56 to 359320, 57 to 220200, 58 to 361420, 59 to 526870, 60 to 427520,
        61 to 105600, 62 to 892970, 63 to 962130, 64 to 252490, 65 to 221100,
        66 to 346110, 67 to 440900, 68 to 648800, 69 to 1332010, 70 to 1276340,
        71 to 1426210, 72 to 1222700, 73 to 1225560, 74 to 620, 75 to 835960,
        76 to 1003590, 77 to 504230, 78 to 367520, 79 to 1145360, 80 to 588650,
        81 to 632360, 82 to 1217060, 83 to 646570, 84 to 1102190, 85 to 1092790,
        86 to 1868140, 87 to 413150, 88 to 1135690, 89 to 972660, 90 to 753640,
        91 to 501300, 92 to 383870, 93 to 1703340, 94 to 210970, 95 to 653530,
        96 to 632470, 97 to 435150, 98 to 560130, 99 to 1659040, 100 to 782330
    )

    // Steam App IDs for free games
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

    private fun steamImg(gameId: Int): String {
        val appId = steamAppIds[gameId] ?: return "https://cdn.akamai.steamstatic.com/steam/apps/1245620/header.jpg"
        return "https://cdn.akamai.steamstatic.com/steam/apps/$appId/header.jpg"
    }

    private fun freeGameImg(name: String): String {
        val appId = freeGameSteamIds[name] ?: return "https://cdn.akamai.steamstatic.com/steam/apps/230410/header.jpg"
        return "https://cdn.akamai.steamstatic.com/steam/apps/$appId/header.jpg"
    }

    fun generateGames(): List<Game> = popularGames.map { info ->
        val basePrices = mapOf(
            "Steam" to (29.99f + (info.id % 40) * 0.5f),
            "Epic Games" to (29.99f + (info.id % 40) * 0.5f - 2f).coerceAtLeast(9.99f),
            "GOG" to (27.99f + (info.id % 40) * 0.5f),
            "EA Play" to (24.99f + (info.id % 30) * 0.4f),
            "Ubisoft+" to (24.99f + (info.id % 30) * 0.4f),
            "Battle.net" to (29.99f + (info.id % 35) * 0.3f),
            "G2A" to (19.99f + (info.id % 30) * 0.4f),
            "Eneba" to (17.99f + (info.id % 25) * 0.4f)
        )
        Game(
            id = info.id,
            name = info.name,
            description = info.description,
            releaseDate = "2024-${String.format("%02d", (info.id % 12) + 1)}-${String.format("%02d", (info.id % 28) + 1)}",
            imageUrl = steamImg(info.id),
            rating = 7.0 + (info.id % 30) * 0.1,
            currentPrices = basePrices,
            historicalDiscount = 10 + (info.id % 80)
        )
    }

    fun generateDiscounts(): List<DiscountedGame> {
        val discounts = mutableListOf<DiscountedGame>()

        // 50 discounted paid games
        for (i in 0 until 50) {
            val game = popularGames[i % popularGames.size]
            val platform = platforms[i % platforms.size]
            val originalPrice = 29.99f + (game.id % 40) * 0.5f
            val discountPct = 15 + (i * 37 % 76) // gives a spread from 15 to 90
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
                    endDate = "2026-06-${String.format("%02d", (i % 28) + 1)}",
                    isHistoricalLowest = discountPct >= 70
                )
            )
        }

        // 23 free games across platforms
        val freeGameNames = listOf(
            "Warframe", "Path of Exile", "Lost Ark", "Albion Online",
            "Old School RuneScape", "Black Desert Online", "Neverwinter",
            "Star Wars: The Old Republic", "Guild Wars 2", "Dauntless",
            "Apex Legends", "Valorant", "Counter-Strike 2", "Halo Infinite Multiplayer",
            "Destiny 2: New Light", "Genshin Impact", "Honkai: Star Rail",
            "World of Tanks", "War Thunder", "Brawlhalla",
            "Rocket League", "Fall Guys", "MultiVersus"
        )
        freeGameNames.forEachIndexed { idx, name ->
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
                    endDate = null,
                    isHistoricalLowest = false
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
