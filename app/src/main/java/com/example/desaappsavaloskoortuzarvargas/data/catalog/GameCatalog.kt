package com.example.desaappsavaloskoortuzarvargas.data.catalog

import com.example.desaappsavaloskoortuzarvargas.domain.model.DLC
import com.example.desaappsavaloskoortuzarvargas.domain.model.Game
import com.example.desaappsavaloskoortuzarvargas.domain.model.News
import com.example.desaappsavaloskoortuzarvargas.domain.model.PriceHistory

/**
 * Curated catalog of popular PC games with metadata.
 * Prices come from real APIs (Steam Store, Epic Games, GOG).
 */
object GameCatalog {

    private data class GameInfo(
        val id: Int,
        val name: String,
        val description: String,
        val tags: List<String> = emptyList(),
        val platforms: List<String> = listOf("Steam"),  // default: Steam-only
        val isF2P: Boolean = false
    )

    private val popularGames = listOf(
        GameInfo(1, "Elden Ring", "An action role-playing game developed by FromSoftware and published by Bandai Namco.", listOf("Action", "RPG", "Open World", "Souls-like")),
        GameInfo(2, "Baldur's Gate 3", "A role-playing game based on the Dungeons & Dragons tabletop role-playing system.", listOf("RPG", "Adventure", "Strategy"), listOf("Steam", "GOG")),
        GameInfo(3, "The Witcher 3: Wild Hunt", "An open world action role-playing game set in a fantasy universe.", listOf("RPG", "Open World", "Action", "Adventure"), listOf("Steam", "Epic Games", "GOG", "Xbox / Microsoft")),
        GameInfo(4, "Cyberpunk 2077", "An open world action-adventure RPG set in the megalopolis of Night City.", listOf("RPG", "FPS", "Open World", "Action"), listOf("Steam", "Epic Games", "GOG", "Xbox / Microsoft")),
        GameInfo(5, "Red Dead Redemption 2", "An epic tale of outlaw life in America's heartland.", listOf("Action", "Adventure", "Open World", "TPS"), listOf("Steam", "Epic Games")),
        GameInfo(6, "God of War Ragnarök", "Kratos and Atreus embark on a mythic journey for answers.", listOf("Action", "Adventure"), listOf("Steam", "Epic Games")),
        GameInfo(7, "Starfield", "An epic space-faring RPG by Bethesda Game Studios.", listOf("RPG", "Open World", "Action", "Adventure"), listOf("Steam", "Xbox / Microsoft")),
        GameInfo(8, "Diablo IV", "A genre-defining action-RPG experience with endless evil to slaughter.", listOf("Action", "RPG", "Co-op"), listOf("Steam", "Battle.net")),
        // Alan Wake 2 — Epic Games EXCLUSIVE (never on Steam)
        GameInfo(9, "Alan Wake 2", "A survival horror game with a mind-bending story.", listOf("Horror", "Survival", "Action", "Adventure"), listOf("Epic Games")),
        GameInfo(10, "Hogwarts Legacy", "An open-world action RPG set in the Wizarding World of Harry Potter.", listOf("RPG", "Open World", "Action", "Adventure"), listOf("Steam", "Epic Games", "Xbox / Microsoft")),
        GameInfo(11, "Resident Evil 4 Remake", "A reimagining of the 2005 survival horror classic.", listOf("Horror", "Survival", "Action", "TPS"), listOf("Steam", "Epic Games")),
        GameInfo(12, "Death Stranding", "An action game by Kojima Productions connecting isolated cities.", listOf("Action", "Adventure", "Open World"), listOf("Steam", "Epic Games")),
        GameInfo(13, "Street Fighter 6", "The next evolution of the legendary fighting game series.", listOf("Fighting", "Action")),
        GameInfo(14, "Tekken 8", "The King of Iron Fist Tournament returns with next-gen visuals.", listOf("Fighting", "Action"), listOf("Steam", "Xbox / Microsoft")),
        GameInfo(15, "Ghost of Tsushima", "An open-world samurai action-adventure game.", listOf("Action", "Adventure", "Open World"), listOf("Steam", "Epic Games")),
        GameInfo(16, "The Last of Us Part I", "A remake of the post-apocalyptic action-adventure game.", listOf("Action", "Adventure", "Horror", "Survival"), listOf("Steam", "Epic Games")),
        GameInfo(17, "Spider-Man Remastered", "Be the Marvel superhero Spider-Man in a new open-world adventure.", listOf("Action", "Adventure", "Open World"), listOf("Steam", "Epic Games")),
        GameInfo(18, "Horizon Zero Dawn", "Explore a vibrant world populated by mysterious machines.", listOf("Action", "RPG", "Open World", "Adventure"), listOf("Steam", "Epic Games", "GOG")),
        GameInfo(19, "Dead Space Remake", "A sci-fi survival horror classic rebuilt from the ground up.", listOf("Horror", "Survival", "Action", "TPS"), listOf("Steam", "EA")),
        GameInfo(20, "Like a Dragon: Infinite Wealth", "A globe-trotting action-adventure RPG.", listOf("RPG", "Action", "Adventure"), listOf("Steam", "Xbox / Microsoft")),
        GameInfo(21, "Final Fantasy VII Rebirth", "The next chapter in the FFVII remake trilogy.", listOf("RPG", "Action", "Adventure"), listOf("Steam", "Epic Games")),
        GameInfo(22, "Persona 5 Royal", "Don the mask and join the Phantom Thieves of Hearts.", listOf("RPG", "Adventure"), listOf("Steam", "Epic Games", "GOG", "Xbox / Microsoft")),
        GameInfo(23, "Metaphor: ReFantazio", "A fantasy RPG from the creators of Persona.", listOf("RPG", "Strategy", "Adventure"), listOf("Steam", "GOG", "Xbox / Microsoft")),
        GameInfo(24, "Palworld", "A creature-collecting open-world survival crafting game.", listOf("Survival", "Open World", "Co-op", "Action"), listOf("Steam", "Xbox / Microsoft")),
        GameInfo(25, "Black Myth: Wukong", "An action RPG rooted in Chinese mythology.", listOf("Action", "RPG", "Souls-like"), listOf("Steam", "Epic Games", "Xbox / Microsoft")),
        GameInfo(26, "S.T.A.L.K.E.R. 2", "A first-person shooter set in the Chornobyl Exclusion Zone.", listOf("FPS", "Horror", "Survival", "Open World"), listOf("Steam", "Xbox / Microsoft")),
        GameInfo(27, "Dragon's Dogma 2", "An open-world action RPG by Capcom.", listOf("RPG", "Action", "Open World", "Adventure")),
        GameInfo(28, "Sekiro: Shadows Die Twice", "A dark and brutal action-adventure from FromSoftware.", listOf("Action", "Souls-like", "Adventure"), listOf("Steam", "GOG")),
        GameInfo(29, "Indiana Jones and the Great Circle", "An action-adventure in first person.", listOf("Action", "Adventure", "FPS"), listOf("Steam", "Xbox / Microsoft")),
        GameInfo(30, "Senua's Saga: Hellblade II", "An intense action-adventure with stunning visuals.", listOf("Action", "Adventure", "Narrative"), listOf("Steam", "Xbox / Microsoft")),
        GameInfo(31, "Monster Hunter: World", "Hunt massive monsters in a living, breathing ecosystem.", listOf("Action", "RPG", "Co-op")),
        GameInfo(32, "Dark Souls III", "An action RPG set in a dark fantasy world.", listOf("Action", "RPG", "Souls-like")),
        GameInfo(33, "Armored Core VI", "Mech combat action from FromSoftware.", listOf("Action", "Simulation"), listOf("Steam", "Xbox / Microsoft")),
        GameInfo(34, "Jedi: Survivor", "Continue Cal Kestis' journey in this action RPG.", listOf("Action", "Adventure", "RPG"), listOf("Steam", "Epic Games", "EA")),
        GameInfo(35, "EA Sports FC 25", "The latest football simulation game.", listOf("Sports", "Simulation"), listOf("Steam", "Epic Games", "EA")),
        GameInfo(36, "Madden NFL 25", "The premier American football simulation.", listOf("Sports", "Simulation"), listOf("Steam", "EA")),
        GameInfo(37, "NBA 2K25", "The definitive basketball simulation experience.", listOf("Sports", "Simulation")),
        GameInfo(38, "F1 24", "Experience the thrill of Formula 1 racing.", listOf("Racing", "Simulation", "Sports"), listOf("Steam", "Epic Games", "EA")),
        GameInfo(39, "WWE 2K24", "Step into the squared circle in this wrestling game.", listOf("Sports", "Fighting")),
        GameInfo(40, "The Crew Motorfest", "A festival open-world racing game.", listOf("Racing", "Open World"), listOf("Steam", "Epic Games", "Ubisoft")),
        GameInfo(41, "Need for Speed Unbound", "Street racing with artistic visual style.", listOf("Racing", "Action"), listOf("Steam", "Epic Games", "EA")),
        GameInfo(42, "Forza Motorsport", "Premium racing simulation with stunning visuals.", listOf("Racing", "Simulation"), listOf("Steam", "Xbox / Microsoft")),
        GameInfo(43, "Ghostwire: Tokyo", "Save Tokyo from supernatural threats in first person.", listOf("Action", "FPS", "Horror"), listOf("Steam", "Xbox / Microsoft")),
        GameInfo(44, "Control Ultimate Edition", "A supernatural action-adventure game by Remedy.", listOf("Action", "Adventure", "TPS"), listOf("Steam", "Epic Games", "GOG", "Xbox / Microsoft")),
        GameInfo(45, "The Callisto Protocol", "A survival horror game set on Jupiter's moon.", listOf("Horror", "Survival", "Action"), listOf("Steam", "Epic Games")),
        GameInfo(46, "Lies of P", "A Soulslike action RPG inspired by Pinocchio.", listOf("Action", "RPG", "Souls-like"), listOf("Steam", "GOG", "Xbox / Microsoft")),
        GameInfo(47, "Remnant II", "A co-operative third-person survival shooter.", listOf("Action", "TPS", "Co-op", "Survival"), listOf("Steam", "Xbox / Microsoft")),
        GameInfo(48, "Resident Evil Village", "Survival horror returns with Ethan Winters.", listOf("Horror", "Survival", "FPS"), listOf("Steam", "Epic Games")),
        GameInfo(49, "Outlast Trials", "A cooperative psychological horror experience.", listOf("Horror", "Co-op", "Survival"), listOf("Steam", "Epic Games")),
        GameInfo(50, "Phasmophobia", "Cooperative ghost hunting with friends.", listOf("Horror", "Co-op", "Simulation")),
        GameInfo(51, "Lethal Company", "A horror game about scavenging on abandoned moons.", listOf("Horror", "Co-op", "Indie")),
        GameInfo(52, "The Forest", "Survive in a forest after a plane crash.", listOf("Survival", "Horror", "Open World")),
        GameInfo(53, "Subnautica", "Explore an alien underwater world.", listOf("Survival", "Adventure", "Open World"), listOf("Steam", "Epic Games", "Xbox / Microsoft")),
        GameInfo(54, "No Man's Sky", "Explore an infinite procedurally generated universe.", listOf("Survival", "Adventure", "Open World", "Sandbox"), listOf("Steam", "Xbox / Microsoft")),
        GameInfo(55, "Deep Rock Galactic", "Co-op FPS about space-mining dwarves.", listOf("FPS", "Co-op", "Action"), listOf("Steam", "Xbox / Microsoft")),
        GameInfo(56, "Elite Dangerous", "A space simulation MMO.", listOf("Simulation", "MMO", "Open World")),
        GameInfo(57, "Kerbal Space Program", "Build and fly rockets in a realistic space sim.", listOf("Simulation", "Sandbox")),
        GameInfo(58, "Astroneer", "Explore and reshape distant worlds.", listOf("Sandbox", "Adventure", "Co-op"), listOf("Steam", "Xbox / Microsoft")),
        GameInfo(59, "Satisfactory", "Build massive automated factories.", listOf("Building", "Simulation", "Sandbox", "Co-op"), listOf("Steam", "Epic Games")),
        GameInfo(60, "Factorio", "Build and maintain factories on an alien planet.", listOf("Building", "Strategy", "Simulation"), listOf("Steam", "GOG")),
        GameInfo(61, "Terraria", "Dig, fight, explore, build in a 2D world.", listOf("Sandbox", "Action", "Adventure", "Indie")),
        GameInfo(62, "Valheim", "A brutal Viking survival game.", listOf("Survival", "Co-op", "Open World", "Indie"), listOf("Steam", "Xbox / Microsoft")),
        GameInfo(63, "Grounded", "A survival adventure where you're shrunk to ant size.", listOf("Survival", "Co-op", "Adventure"), listOf("Steam", "Xbox / Microsoft")),
        GameInfo(64, "Rust", "A multiplayer survival game.", listOf("Survival", "FPS", "Open World")),
        GameInfo(65, "DayZ", "Post-apocalyptic open-world survival.", listOf("Survival", "FPS", "Open World")),
        GameInfo(66, "Ark: Survival Evolved", "Survive among dinosaurs on a mysterious island.", listOf("Survival", "Open World", "Co-op")),
        GameInfo(67, "Conan Exiles", "Survive and conquer in the world of Conan the Barbarian.", listOf("Survival", "Open World", "Co-op")),
        GameInfo(68, "Raft", "Survive on a tiny raft in the middle of the ocean.", listOf("Survival", "Co-op", "Adventure")),
        GameInfo(69, "Stray", "Explore a cybercity as a stray cat.", listOf("Adventure", "Indie", "Platformer"), listOf("Steam", "Epic Games")),
        GameInfo(70, "Kena: Bridge of Spirits", "A story-driven action-adventure.", listOf("Action", "Adventure", "Indie"), listOf("Steam", "Epic Games")),
        GameInfo(71, "It Takes Two", "A crazy fun co-op adventure.", listOf("Co-op", "Adventure", "Platformer"), listOf("Steam", "Epic Games", "EA")),
        GameInfo(72, "A Way Out", "A cooperative action-adventure game.", listOf("Co-op", "Adventure", "Action"), listOf("Steam", "EA")),
        GameInfo(73, "Unravel Two", "A charming puzzle-platformer for two.", listOf("Puzzle", "Platformer", "Co-op", "Indie"), listOf("Steam", "EA")),
        GameInfo(74, "Portal 2", "The legendary first-person puzzle game.", listOf("Puzzle", "FPS", "Co-op")),
        GameInfo(75, "The Talos Principle 2", "A philosophical first-person puzzle game.", listOf("Puzzle", "Adventure"), listOf("Steam", "GOG")),
        GameInfo(76, "Tetris Effect: Connected", "Tetris reimagined with stunning visuals.", listOf("Puzzle", "Indie"), listOf("Steam", "Epic Games", "Xbox / Microsoft")),
        GameInfo(77, "Celeste", "A challenging platformer about climbing a mountain.", listOf("Platformer", "Indie"), listOf("Steam", "Epic Games", "GOG")),
        GameInfo(78, "Hollow Knight", "A challenging 2D action-adventure in a vast world.", listOf("Action", "Platformer", "Indie", "Souls-like"), listOf("Steam", "Epic Games", "GOG")),
        GameInfo(79, "Hades", "A rogue-like dungeon crawler from Supergiant Games.", listOf("Roguelike", "Action", "Indie"), listOf("Steam", "Epic Games", "GOG")),
        GameInfo(80, "Dead Cells", "A rogue-lite action-platformer.", listOf("Roguelike", "Action", "Platformer", "Indie"), listOf("Steam", "Epic Games", "GOG")),
        GameInfo(81, "Risk of Rain 2", "A third-person roguelike shooter.", listOf("Roguelike", "TPS", "Co-op")),
        GameInfo(82, "Gunfire Reborn", "A FPS roguelite with RPG elements.", listOf("FPS", "Roguelike", "Co-op", "RPG")),
        GameInfo(83, "Slay the Spire", "A roguelike deckbuilding game.", listOf("Roguelike", "Card Game", "Strategy", "Indie"), listOf("Steam", "Epic Games", "GOG")),
        GameInfo(84, "Monster Train", "A strategic roguelike deckbuilder.", listOf("Roguelike", "Card Game", "Strategy")),
        GameInfo(85, "Inscryption", "An inky card-based odyssey.", listOf("Card Game", "Horror", "Puzzle", "Indie")),
        GameInfo(86, "Dave the Diver", "A casual adventure RPG about deep-sea diving.", listOf("Adventure", "RPG", "Indie")),
        GameInfo(87, "Stardew Valley", "Farm, fish, and build community in a charming RPG.", listOf("RPG", "Simulation", "Indie", "Sandbox"), listOf("Steam", "GOG")),
        GameInfo(88, "Unpacking", "A zen puzzle game about the experience of moving.", listOf("Puzzle", "Indie", "Narrative"), listOf("Steam", "Epic Games", "GOG", "Xbox / Microsoft")),
        GameInfo(89, "Spiritfarer", "A cozy management game about dying.", listOf("Adventure", "Simulation", "Indie", "Narrative"), listOf("Steam", "Epic Games", "GOG", "Xbox / Microsoft")),
        GameInfo(90, "Outer Wilds", "Explore a solar system stuck in a time loop.", listOf("Adventure", "Puzzle", "Indie", "Open World"), listOf("Steam", "Epic Games", "GOG")),
        GameInfo(91, "What Remains of Edith Finch", "A collection of stories about a cursed family.", listOf("Adventure", "Narrative", "Indie"), listOf("Steam", "Epic Games", "GOG")),
        GameInfo(92, "Firewatch", "A mystery adventure in the Wyoming wilderness.", listOf("Adventure", "Narrative", "Indie"), listOf("Steam", "GOG")),
        GameInfo(93, "The Stanley Parable: Ultra Deluxe", "A narrative exploration game.", listOf("Adventure", "Narrative", "Indie"), listOf("Steam", "Epic Games")),
        GameInfo(94, "The Witness", "An exploration-puzzle game on a mysterious island.", listOf("Puzzle", "Adventure", "Indie"), listOf("Steam", "Epic Games")),
        GameInfo(95, "Return of the Obra Dinn", "A mystery puzzle game on a ghost ship.", listOf("Puzzle", "Adventure", "Indie", "Narrative"), listOf("Steam", "GOG")),
        GameInfo(96, "Disco Elysium: The Final Cut", "A groundbreaking role-playing game.", listOf("RPG", "Narrative", "Adventure"), listOf("Steam", "GOG")),
        GameInfo(97, "Divinity: Original Sin 2", "A deep tactical RPG with co-op play.", listOf("RPG", "Strategy", "Co-op"), listOf("Steam", "Epic Games", "GOG")),
        GameInfo(98, "Pillars of Eternity II", "An epic RPG adventuring on the high seas.", listOf("RPG", "Strategy", "Adventure"), listOf("Steam", "Epic Games", "GOG")),
        GameInfo(99, "Hitman: World of Assassination", "A stealth assassination sandbox.", listOf("Action", "Adventure", "Open World"), listOf("Steam", "Epic Games", "GOG")),
        GameInfo(100, "Doom Eternal", "A fast-paced first-person shooter.", listOf("FPS", "Action"), listOf("Steam", "Xbox / Microsoft", "Battle.net")),
        // ═══ F2P games ═══
        // Riot Games exclusives — NOT on Steam
        GameInfo(101, "League of Legends", "A team-based strategy game where two teams of five champions face off to destroy the other's base.", listOf("Free2Play", "MOBA", "Strategy"), listOf("Riot Games"), isF2P = true),
        GameInfo(102, "Valorant", "A 5v5 character-based tactical FPS where precise gunplay meets unique agent abilities.", listOf("Free2Play", "FPS", "Tactical"), listOf("Riot Games"), isF2P = true),
        GameInfo(113, "Teamfight Tactics", "An auto-battler strategy game from Riot Games set in the League of Legends universe.", listOf("Free2Play", "Strategy", "Auto-Battler"), listOf("Riot Games"), isF2P = true),
        // Epic Games exclusives — NOT on Steam
        GameInfo(103, "Fortnite", "A free-to-play Battle Royale game with building mechanics and constant content updates.", listOf("Free2Play", "Battle Royale", "TPS", "Building"), listOf("Epic Games"), isF2P = true),
        GameInfo(111, "Rocket League", "A high-powered hybrid of arcade-style soccer and vehicular mayhem.", listOf("Free2Play", "Sports", "Racing"), listOf("Epic Games"), isF2P = true),
        // HoYoverse games — NOT on Steam (own launcher + Epic)
        GameInfo(107, "Genshin Impact", "An open-world action RPG with gacha mechanics set in the fantasy world of Teyvat.", listOf("Free2Play", "RPG", "Open World", "Action"), listOf("Epic Games", "HoYoverse"), isF2P = true),
        GameInfo(114, "Honkai: Star Rail", "A space fantasy RPG with strategic turn-based combat.", listOf("Free2Play", "RPG"), listOf("Epic Games", "HoYoverse"), isF2P = true),
        // F2P games that ARE on Steam
        GameInfo(104, "Marvel Rivals", "A team-based PvP shooter set in the Marvel universe with iconic heroes and villains.", listOf("Free2Play", "FPS", "Action", "Co-op"), listOf("Steam", "Epic Games"), isF2P = true),
        GameInfo(105, "Dota 2", "A complex MOBA where two teams of five players compete to destroy the opponent's Ancient.", listOf("Free2Play", "MOBA", "Strategy"), listOf("Steam"), isF2P = true),
        GameInfo(106, "Apex Legends", "A free-to-play hero shooter with strategic squad-based Battle Royale gameplay.", listOf("Free2Play", "FPS", "Battle Royale"), listOf("Steam", "EA"), isF2P = true),
        GameInfo(108, "Counter-Strike 2", "The definitive competitive tactical FPS, successor to CS:GO.", listOf("Free2Play", "FPS", "Tactical"), listOf("Steam"), isF2P = true),
        GameInfo(109, "Warframe", "A cooperative free-to-play third-person online action game set in an evolving sci-fi world.", listOf("Free2Play", "TPS", "Co-op", "Action"), listOf("Steam"), isF2P = true),
        GameInfo(110, "Path of Exile", "A free-to-play dark fantasy action RPG with deep character customization.", listOf("Free2Play", "RPG", "Action"), listOf("Steam"), isF2P = true),
        GameInfo(112, "Fall Guys", "A massively multiplayer party game with hilarious obstacle courses.", listOf("Free2Play", "Platformer", "Party"), listOf("Steam", "Epic Games"), isF2P = true),
        // ═══ New 2024-2025 releases ═══
        GameInfo(115, "Monster Hunter Wilds", "Hunt breathtaking monsters in expansive biomes that change with the weather.", listOf("Action", "RPG", "Co-op"), listOf("Steam", "Epic Games", "Xbox / Microsoft")),
        GameInfo(116, "Clair Obscur: Expedition 33", "A stunning French RPG set in a surreal Art Deco world facing extinction.", listOf("RPG", "Turn-Based", "Adventure"), listOf("Steam", "Epic Games", "Xbox / Microsoft")),
        GameInfo(117, "Kingdom Come: Deliverance 2", "Continue Henry's journey in a breathtaking medieval open world.", listOf("RPG", "Open World", "Adventure", "Action"), listOf("Steam", "Epic Games", "Xbox / Microsoft")),
        GameInfo(118, "Warhammer 40K: Space Marine 2", "Don your power armor and fight the alien swarms threatening humanity.", listOf("Action", "TPS", "Co-op"), listOf("Steam", "Epic Games", "Xbox / Microsoft")),
        GameInfo(119, "Silent Hill 2 Remake", "A faithful remake of the psychological horror masterpiece.", listOf("Horror", "Survival", "TPS"), listOf("Steam")),
        GameInfo(120, "Dragon Age: The Veilguard", "Lead a crew of seven heroes to save the world of Thedas.", listOf("RPG", "Action", "Adventure"), listOf("Steam", "EA", "Epic Games")),
        GameInfo(121, "Avowed", "Explore the Living Lands — a dark, magic-infused open world.", listOf("RPG", "FPS", "Action", "Open World"), listOf("Steam", "Xbox / Microsoft")),
        GameInfo(122, "The Elder Scrolls IV: Oblivion Remastered", "The iconic RPG classic rebuilt with stunning next-gen visuals.", listOf("RPG", "Open World", "Action", "Adventure"), listOf("Steam", "Xbox / Microsoft")),
        GameInfo(123, "Path of Exile 2", "A next-gen dark fantasy action RPG with deep build depth.", listOf("Action", "RPG"), listOf("Steam")),
        GameInfo(124, "Helldivers 2", "Squad up and fight for democracy against overwhelming alien forces.", listOf("Action", "TPS", "Co-op"), listOf("Steam")),
        GameInfo(125, "Doom: The Dark Ages", "Return to the roots of DOOM with medieval brutality and power fantasy.", listOf("FPS", "Action"), listOf("Steam", "Xbox / Microsoft")),
        GameInfo(126, "Age of Mythology: Retold", "The classic real-time strategy game reimagined with modern visuals.", listOf("Strategy", "RTS"), listOf("Steam", "Xbox / Microsoft")),
        GameInfo(127, "Like a Dragon: Pirate Yakuza in Hawaii", "Kiryu and Majima together in a swashbuckling Hawaiian adventure.", listOf("Action", "RPG", "Adventure"), listOf("Steam", "Xbox / Microsoft")),
        GameInfo(128, "Civilization VII", "Lead humanity from the ancient era to the modern age.", listOf("Strategy", "Turn-Based"), listOf("Steam", "Epic Games")),
        GameInfo(129, "Assassin's Creed Shadows", "Live the fantasy of the shinobi and the samurai in feudal Japan.", listOf("Action", "Stealth", "Open World", "Adventure"), listOf("Steam", "Epic Games", "Xbox / Microsoft", "Ubisoft")),
        GameInfo(130, "Balatro", "A hypnotic roguelike card game — poker meets Slay the Spire.", listOf("Roguelike", "Card Game", "Indie"), listOf("Steam", "Epic Games", "Xbox / Microsoft")),
        GameInfo(131, "Hades II", "The sequel to the award-winning rogue-like dungeon crawler.", listOf("Roguelike", "Action", "Indie")),
        // ═══ Classic popular games ═══
        GameInfo(132, "Half-Life: Alyx", "Valve's VR return to the Half-Life universe — a masterpiece.", listOf("FPS", "VR", "Horror", "Action"), listOf("Steam")),
        GameInfo(133, "Prey (2017)", "Explore a space station overrun by shapeshifting aliens in this immersive sim.", listOf("FPS", "Horror", "Action", "Adventure"), listOf("Steam", "Xbox / Microsoft")),
        GameInfo(134, "Dishonored 2", "Master Emily and Corvo's supernatural powers in a beautifully crafted steampunk world.", listOf("Action", "Stealth", "FPS", "Adventure"), listOf("Steam", "Xbox / Microsoft")),
        GameInfo(135, "Batman: Arkham Knight", "The epic conclusion to the Arkham trilogy — with the Batmobile.", listOf("Action", "Adventure", "TPS"), listOf("Steam")),
        GameInfo(136, "Mass Effect Legendary Edition", "The complete legendary sci-fi trilogy remastered in 4K.", listOf("RPG", "Action", "Sci-fi", "Adventure"), listOf("Steam", "EA", "Xbox / Microsoft")),
        GameInfo(137, "BioShock Remastered", "Descend into the underwater dystopia of Rapture.", listOf("FPS", "Horror", "Action", "Adventure"), listOf("Steam", "Xbox / Microsoft")),
        GameInfo(138, "Star Wars Jedi: Fallen Order", "Cal Kestis hunts for a Jedi holocron in this action-adventure.", listOf("Action", "Adventure", "TPS"), listOf("Steam", "Epic Games", "EA", "Xbox / Microsoft")),
        GameInfo(139, "Titanfall 2", "Best-in-class FPS multiplayer and one of gaming's greatest single-player campaigns.", listOf("FPS", "Action"), listOf("Steam", "EA")),
        GameInfo(140, "Warhammer 40K: Boltgun", "A love letter to classic arena shooters set in the 40K universe.", listOf("FPS", "Action", "Indie"), listOf("Steam", "Xbox / Microsoft"))
    )

    private val platforms = listOf("Steam", "Epic Games", "GOG", "Xbox / Microsoft", "EA", "Ubisoft", "Battle.net")

    // GOG game URLs — game ID → direct product page URL.
    // Slugs verified from the GOG catalog. Base URL: https://www.gog.com/en/game/{slug}
    // These override the live API result to prevent wrong matches (e.g. The Witcher 3
    // appearing as a promoted/sponsored item in unrelated search result sets).
    private val gogGameUrls = mapOf(
        2  to "https://www.gog.com/en/game/baldurs_gate_3",                          // Baldur's Gate 3
        3  to "https://www.gog.com/en/game/the_witcher_3_wild_hunt_goty",            // The Witcher 3: Wild Hunt
        4  to "https://www.gog.com/en/game/cyberpunk_2077",                          // Cyberpunk 2077
        18 to "https://www.gog.com/en/game/horizon_zero_dawn_complete_edition",      // Horizon Zero Dawn
        22 to "https://www.gog.com/en/game/persona_5_royal",                         // Persona 5 Royal
        23 to "https://www.gog.com/en/game/metaphor_refantazio",                     // Metaphor: ReFantazio
        28 to "https://www.gog.com/en/game/sekiro_shadows_die_twice",                // Sekiro: Shadows Die Twice
        44 to "https://www.gog.com/en/game/control_ultimate_edition",                // Control Ultimate Edition (+ Xbox / Microsoft)
        46 to "https://www.gog.com/en/game/lies_of_p",                               // Lies of P
        60 to "https://www.gog.com/en/game/factorio",                                // Factorio
        75 to "https://www.gog.com/en/game/the_talos_principle_2",                   // The Talos Principle 2
        77 to "https://www.gog.com/en/game/celeste",                                  // Celeste
        78 to "https://www.gog.com/en/game/hollow_knight",                            // Hollow Knight
        79 to "https://www.gog.com/en/game/hades",                                   // Hades
        80 to "https://www.gog.com/en/game/dead_cells",                              // Dead Cells
        83 to "https://www.gog.com/en/game/slay_the_spire",                          // Slay the Spire
        87 to "https://www.gog.com/en/game/stardew_valley",                          // Stardew Valley
        88 to "https://www.gog.com/en/game/unpacking",                               // Unpacking
        89 to "https://www.gog.com/en/game/spiritfarer_farewell_edition",            // Spiritfarer
        90 to "https://www.gog.com/en/game/outer_wilds",                             // Outer Wilds
        91 to "https://www.gog.com/en/game/what_remains_of_edith_finch",             // What Remains of Edith Finch
        92 to "https://www.gog.com/en/game/firewatch",                               // Firewatch
        95 to "https://www.gog.com/en/game/return_of_the_obra_dinn",                 // Return of the Obra Dinn
        96 to "https://www.gog.com/en/game/disco_elysium_the_final_cut",             // Disco Elysium: The Final Cut
        97 to "https://www.gog.com/en/game/divinity_original_sin_2_developers_cut",  // Divinity: Original Sin 2
        98 to "https://www.gog.com/en/game/pillars_of_eternity_2_deadfire",          // Pillars of Eternity II
        99 to "https://www.gog.com/en/game/hitman_world_of_assassination"            // Hitman: World of Assassination
    )

    // Epic Games Store page slugs — game catalog ID → verified Epic URL slug.
    // These override the urlSlug returned by Epic's GraphQL API, which can sometimes
    // return a UUID-like offer ID instead of the human-readable page slug.
    // Verified against: https://store.epicgames.com/en-US/p/{slug}
    private val epicSlugs = mapOf(
        1  to "elden-ring",                        // Elden Ring
        2  to "baldurs-gate-3",                    // Baldur's Gate 3
        3  to "the-witcher-3-wild-hunt",           // The Witcher 3: Wild Hunt
        4  to "cyberpunk-2077",                    // Cyberpunk 2077 ← link corregido
        5  to "red-dead-redemption-2",             // Red Dead Redemption 2
        6  to "god-of-war-ragnarok",               // God of War Ragnarök
        9  to "alan-wake-2",                       // Alan Wake 2
        10 to "hogwarts-legacy",                   // Hogwarts Legacy
        11 to "resident-evil-4",                   // Resident Evil 4 Remake
        12 to "death-stranding",                   // Death Stranding
        15 to "ghost-of-tsushima",                 // Ghost of Tsushima
        16 to "the-last-of-us-part-i",             // The Last of Us Part I
        17 to "spider-man-remastered",             // Spider-Man Remastered
        18 to "horizon-zero-dawn-remastered",      // Horizon Zero Dawn
        21 to "final-fantasy-vii-rebirth",         // Final Fantasy VII Rebirth
        22 to "persona-5-royal",                   // Persona 5 Royal
        23 to "metaphor-refantazio",               // Metaphor: ReFantazio
        24 to "palworld",                          // Palworld
        25 to "black-myth-wukong",                 // Black Myth: Wukong
        103 to "fortnite",                         // Fortnite (F2P)
        107 to "genshin-impact",                   // Genshin Impact (F2P)
        111 to "rocketleague",                     // Rocket League (F2P)
        114 to "honkai-star-rail",                 // Honkai: Star Rail (F2P)
        115 to "monster-hunter-wilds",             // Monster Hunter Wilds
        116 to "clair-obscur-expedition-33",       // Clair Obscur: Expedition 33
        117 to "kingdom-come-deliverance-2",       // Kingdom Come: Deliverance 2
        124 to "helldivers-2",                     // Helldivers 2
        125 to "doom-the-dark-ages",               // Doom: The Dark Ages
        129 to "assassins-creed-shadows",          // Assassin's Creed Shadows
        130 to "balatro",                          // Balatro
        131 to "hades-ii"                          // Hades II
    )

    // Ubisoft Store AR URLs — game ID → direct product page URL.
    // Format: https://store.ubisoft.com/ofertas/{slug}/{productId}.html?lang=es_AR
    private val ubisoftUrls = mapOf(
        40 to "https://store.ubisoft.com/ofertas/the-crew-motorfest/63bc67dbd406ab22f1174305.html?lang=es_AR",
        129 to "https://store.ubisoft.com/ofertas/assassins-creed-shadows/660e5a03fbff4e2940488bcd.html?lang=es_AR"
    )

    // Xbox / Microsoft Store product IDs (game catalog ID → Xbox product ID).
    // Extracted from official Xbox store product URLs:
    // https://www.xbox.com/es-AR/games/store/{title-slug}/{productId}
    private val xboxProductIds = mapOf(
        7   to "BQ2VSWP8LLS1",  // Starfield
        10  to "9NKX9X90NWSM",  // Hogwarts Legacy
        14  to "9NFBWVSC88M5",  // Tekken 8
        20  to "9MV0B2ZFQJQT",  // Like a Dragon: Infinite Wealth
        22  to "9P7T5B0BWJKF",  // Persona 5 Royal
        23  to "9PP9J5S3L1CK",  // Metaphor: ReFantazio
        24  to "9MT4TQRJRP3W",  // Palworld
        25  to "9P5X4QVZCG54",  // Black Myth: Wukong
        26  to "9PDWD28DJCPR",  // S.T.A.L.K.E.R. 2
        29  to "9NB37X38B8DL",  // Indiana Jones and the Great Circle
        33  to "9N5TT8MXBZ5S",  // Armored Core VI
        42  to "9NS4CZNTPD7G",  // Forza Motorsport
        47  to "9PB9QMD8BWM6",  // Remnant II
        53  to "9NF83PRZK6SR",  // Subnautica
        62  to "9N3NLJ1QZQC2",  // Valheim
        115 to "9P9N26ZBLQS3",  // Monster Hunter Wilds
        116 to "9N3VP43FWX8F",  // Clair Obscur: Expedition 33
        117 to "9NC8NX9MQX6N",  // Kingdom Come: Deliverance 2
        118 to "9N25LB1TXSSS",  // Warhammer 40K: Space Marine 2
        121 to "9MWLPPWMBL3G",  // Avowed
        122 to "9NZQJPD3QFB9",  // Oblivion Remastered
        125 to "9P59Q3QTCP54",  // Doom: The Dark Ages
        129 to "9NBLGGH5K6VB",  // Assassin's Creed Shadows
        138 to "c2csdtscbz0c"   // Star Wars Jedi: Fallen Order
    )

    // Xbox title hints — game catalog ID → localized title used in Argentine Xbox Store.
    // Used when the English game name doesn't match the Spanish Xbox listing.
    private val xboxTitleHints = mapOf(
        138 to "Star Wars Jedi: La Orden Caída"
    )

    // EA App / Origin game URLs — game catalog ID → EA product page URL.
    private val eaGameUrls = mapOf(
        19  to "https://www.ea.com/es/games/dead-space",                     // Dead Space Remake
        34  to "https://www.ea.com/es/games/starwars/jedi-survivor",         // Jedi: Survivor
        35  to "https://www.ea.com/es/games/ea-sports-fc",                   // EA Sports FC 25
        36  to "https://www.ea.com/es/games/madden-nfl",                     // Madden NFL 25
        38  to "https://www.ea.com/es/games/f1/f1-24",                       // F1 24
        41  to "https://www.ea.com/es/games/need-for-speed/need-for-speed-unbound", // NFS Unbound
        71  to "https://www.ea.com/es/games/it-takes-two",                   // It Takes Two
        72  to "https://www.ea.com/es/games/a-way-out",                      // A Way Out
        120 to "https://www.ea.com/es/games/dragon-age/dragon-age-the-veilguard",  // Dragon Age: Veilguard
        136 to "https://www.ea.com/es/games/mass-effect/mass-effect-legendary-edition", // ME Legendary
        138 to "https://www.ea.com/es/games/starwars/jedi-fallen-order",     // Star Wars Jedi: Fallen Order
        139 to "https://www.ea.com/es/games/titanfall/titanfall-2"           // Titanfall 2
    )

    // Steam App IDs — ONLY for games that are actually on Steam.
    // Alan Wake 2, Valorant, Genshin Impact, Honkai: Star Rail are NOT on Steam.
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
        96 to 632470, 97 to 435150, 98 to 560130, 99 to 1659040, 100 to 782330,
        // F2P games that ARE on Steam
        104 to 2767030, 105 to 570, 106 to 1172470,
        108 to 730, 109 to 230410, 110 to 238960, 112 to 1097150,
        // 2024-2025 new releases
        115 to 2246340, // Monster Hunter Wilds
        116 to 1903340, // Clair Obscur: Expedition 33
        117 to 1771300, // Kingdom Come: Deliverance 2
        118 to 2183900, // Warhammer 40K: Space Marine 2
        119 to 2124490, // Silent Hill 2 Remake
        120 to 1845910, // Dragon Age: The Veilguard
        121 to 2457220, // Avowed
        122 to 2748220, // The Elder Scrolls IV: Oblivion Remastered
        123 to 2694490, // Path of Exile 2
        124 to 553850,  // Helldivers 2
        125 to 3017860, // Doom: The Dark Ages
        126 to 1934680, // Age of Mythology: Retold
        127 to 3061810, // Like a Dragon: Pirate Yakuza in Hawaii
        128 to 1295660, // Civilization VII (Sid Meier's)
        129 to 3159330, // Assassin's Creed Shadows
        130 to 2379780, // Balatro
        131 to 1145350, // Hades II
        // ═══ Classic popular games ═══
        132 to 546560,  // Half-Life: Alyx
        133 to 480490,  // Prey (2017)
        134 to 403640,  // Dishonored 2
        135 to 208650,  // Batman: Arkham Knight
        136 to 1328670, // Mass Effect Legendary Edition
        137 to 409710,  // BioShock Remastered
        138 to 1172380, // Star Wars Jedi: Fallen Order
        139 to 1237970, // Titanfall 2
        140 to 1824580  // Warhammer 40K: Boltgun
    )

    // Games NOT on Steam — image will be fetched at runtime from their store API.
    // Empty string = runtime fetch from Epic GraphQL (for Epic/HoYoverse games).
    // Riot DDragon URLs are used for Riot-exclusive games as reliable static fallback.
    private val alternativeImages = mapOf(
        // Alan Wake 2 — Epic Games exclusive
        9 to "",
        // League of Legends — Riot Games exclusive
        101 to "https://ddragon.leagueoflegends.com/cdn/img/champion/splash/Ahri_0.jpg",
        // Valorant — Riot Games exclusive
        102 to "https://ddragon.leagueoflegends.com/cdn/img/champion/splash/Jett_0.jpg",
        // Fortnite — Epic Games exclusive
        103 to "",
        // Genshin Impact — HoYoverse / Epic Games
        107 to "",
        // Rocket League — Epic Games exclusive
        111 to "",
        // TFT — Riot Games exclusive
        113 to "https://ddragon.leagueoflegends.com/cdn/img/champion/splash/Jinx_0.jpg",
        // Honkai: Star Rail — HoYoverse / Epic Games
        114 to ""
    )

    private fun steamImg(gameId: Int): String {
        // Check if game has a non-empty alternative image (not on Steam)
        alternativeImages[gameId]?.takeIf { it.isNotEmpty() }?.let { return it }
        // If game has a Steam App ID, use Steam CDN
        val appId = steamAppIds[gameId]
            ?: return ""  // No Steam ID → image will be fetched at runtime from the game's store
        return "https://cdn.akamai.steamstatic.com/steam/apps/$appId/header.jpg"
    }

    // Temporarily free games (limited time promotions, not permanently F2P)
    private data class TempFreeGame(val name: String, val steamId: Int, val platform: String, val endDate: String)
    private val temporarilyFreeGames = listOf(
        TempFreeGame("Tomb Raider (2013)", 203160, "Epic Games", "2026-05-18"),
        TempFreeGame("Stellaris", 281990, "Epic Games", "2026-05-20"),
        TempFreeGame("For Honor", 304390, "Ubisoft", "2026-05-22"),
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

    private val dlcSteamIds = mapOf(
        1001 to 2778580, 1002 to 1905880, 1003 to 378648, 1004 to 378649,
        1005 to 1151641, 1006 to 2344521, 1007 to 2215431, 1008 to 1880360,
        1009 to 1118010, 1010 to 506970, 1011 to 506971, 1012 to 558551,
        1013 to 753641, 1014 to 870781, 1015 to 870782, 1016 to 435151,
        1017 to 1659041, 1018 to 1098291, 1019 to 1098292, 1020 to 1196591
    )


    /**
     * Historical lowest discount percentages from SteamDB.
     * Only for games available on Steam — sourced from SteamDB historical low data.
     * Games NOT on Steam (e.g. Alan Wake 2 = Epic exclusive) are absent from this map
     * and will display 0% (historical tracking from our own price_history DB).
     */
    private val steamDbHistoricalDiscount = mapOf(
        // ── AAA / Big releases ──
        1 to 50,    // Elden Ring — 50% (SteamDB hist low)
        2 to 30,    // Baldur's Gate 3 — 30%
        3 to 90,    // The Witcher 3: Wild Hunt — 90%
        4 to 75,    // Cyberpunk 2077 — 75%
        5 to 80,    // Red Dead Redemption 2 — 80%
        6 to 40,    // God of War Ragnarök — 40%
        7 to 50,    // Starfield — 50%
        8 to 50,    // Diablo IV — 50%
        // 9 = Alan Wake 2 — Epic exclusive, not on Steam → absent
        10 to 75,   // Hogwarts Legacy — 75%
        11 to 67,   // Resident Evil 4 Remake — 67%
        12 to 80,   // Death Stranding — 80%
        13 to 60,   // Street Fighter 6 — 60%
        14 to 50,   // Tekken 8 — 50%
        15 to 50,   // Ghost of Tsushima — 50%
        16 to 60,   // The Last of Us Part I — 60%
        17 to 60,   // Spider-Man Remastered — 60%
        18 to 80,   // Horizon Zero Dawn — 80%
        19 to 80,   // Dead Space Remake — 80%
        20 to 50,   // Like a Dragon: Infinite Wealth — 50%
        21 to 30,   // Final Fantasy VII Rebirth — 30%
        22 to 50,   // Persona 5 Royal — 50%
        23 to 30,   // Metaphor: ReFantazio — 30%
        24 to 50,   // Palworld — 50%
        25 to 25,   // Black Myth: Wukong — 25%
        26 to 30,   // S.T.A.L.K.E.R. 2 — 30%
        27 to 50,   // Dragon's Dogma 2 — 50%
        28 to 75,   // Sekiro: Shadows Die Twice — 75%
        29 to 30,   // Indiana Jones and the Great Circle — 30%
        30 to 40,   // Senua's Saga: Hellblade II — 40%
        31 to 80,   // Monster Hunter: World — 80%
        32 to 85,   // Dark Souls III — 85%
        33 to 50,   // Armored Core VI — 50%
        34 to 75,   // Jedi: Survivor — 75%
        35 to 60,   // EA Sports FC 25 — 60%
        36 to 67,   // Madden NFL 25 — 67%
        37 to 67,   // NBA 2K25 — 67%
        38 to 75,   // F1 24 — 75%
        39 to 67,   // WWE 2K24 — 67%
        40 to 75,   // The Crew Motorfest — 75%
        41 to 85,   // Need for Speed Unbound — 85%
        42 to 50,   // Forza Motorsport — 50%
        // ── Horror / Survival ──
        43 to 80,   // Ghostwire: Tokyo — 80%
        44 to 90,   // Control Ultimate Edition — 90%
        45 to 90,   // The Callisto Protocol — 90%
        46 to 60,   // Lies of P — 60%
        47 to 60,   // Remnant II — 60%
        48 to 75,   // Resident Evil Village — 75%
        49 to 60,   // Outlast Trials — 60%
        50 to 50,   // Phasmophobia — 50%
        51 to 40,   // Lethal Company — 40%
        52 to 85,   // The Forest — 85%
        53 to 75,   // Subnautica — 75%
        54 to 60,   // No Man's Sky — 60%
        55 to 75,   // Deep Rock Galactic — 75%
        // ── Simulation / Sandbox ──
        56 to 85,   // Elite Dangerous — 85%
        57 to 80,   // Kerbal Space Program — 80%
        58 to 70,   // Astroneer — 70%
        59 to 50,   // Satisfactory — 50%
        60 to 35,   // Factorio — 35%
        61 to 50,   // Terraria — 50%
        62 to 50,   // Valheim — 50%
        63 to 50,   // Grounded — 50%
        64 to 67,   // Rust — 67%
        65 to 75,   // DayZ — 75%
        66 to 90,   // Ark: Survival Evolved — 90%
        67 to 80,   // Conan Exiles — 80%
        68 to 60,   // Raft — 60%
        // ── Indie / Adventure ──
        69 to 60,   // Stray — 60%
        70 to 70,   // Kena: Bridge of Spirits — 70%
        71 to 75,   // It Takes Two — 75%
        72 to 90,   // A Way Out — 90%
        73 to 90,   // Unravel Two — 90%
        74 to 90,   // Portal 2 — 90%
        75 to 50,   // The Talos Principle 2 — 50%
        76 to 75,   // Tetris Effect: Connected — 75%
        77 to 75,   // Celeste — 75%
        78 to 75,   // Hollow Knight — 75%
        79 to 60,   // Hades — 60%
        80 to 75,   // Dead Cells — 75%
        81 to 80,   // Risk of Rain 2 — 80%
        82 to 60,   // Gunfire Reborn — 60%
        83 to 75,   // Slay the Spire — 75%
        84 to 70,   // Monster Train — 70%
        85 to 60,   // Inscryption — 60%
        86 to 50,   // Dave the Diver — 50%
        87 to 60,   // Stardew Valley — 60%
        88 to 60,   // Unpacking — 60%
        89 to 80,   // Spiritfarer — 80%
        90 to 60,   // Outer Wilds — 60%
        91 to 80,   // What Remains of Edith Finch — 80%
        92 to 80,   // Firewatch — 80%
        93 to 50,   // The Stanley Parable: Ultra Deluxe — 50%
        94 to 75,   // The Witness — 75%
        95 to 60,   // Return of the Obra Dinn — 60%
        96 to 90,   // Disco Elysium: The Final Cut — 90%
        97 to 85,   // Divinity: Original Sin 2 — 85%
        98 to 80,   // Pillars of Eternity II — 80%
        99 to 80,    // Hitman: World of Assassination — 80%
        100 to 80,   // Doom Eternal — 80%
        // ── 2024-2025 new releases ──
        115 to 0,    // Monster Hunter Wilds — too new
        116 to 0,    // Clair Obscur: Expedition 33 — too new
        117 to 20,   // Kingdom Come: Deliverance 2 — 20%
        118 to 25,   // Warhammer 40K: Space Marine 2 — 25%
        119 to 30,   // Silent Hill 2 Remake — 30%
        120 to 40,   // Dragon Age: The Veilguard — 40%
        121 to 20,   // Avowed — 20%
        122 to 0,    // Oblivion Remastered — too new
        123 to 0,    // Path of Exile 2 — Early Access
        124 to 33,   // Helldivers 2 — 33%
        125 to 0,    // Doom: The Dark Ages — too new
        126 to 33,   // Age of Mythology: Retold — 33%
        127 to 20,   // Like a Dragon: Pirate Yakuza in Hawaii — 20%
        128 to 20,   // Civilization VII — 20%
        129 to 20,   // Assassin's Creed Shadows — 20%
        130 to 20,   // Balatro — 20%
        131 to 0,    // Hades II — Early Access
        // ── Classics ──
        132 to 40,   // Half-Life: Alyx — 40%
        133 to 90,   // Prey (2017) — 90%
        134 to 90,   // Dishonored 2 — 90%
        135 to 90,   // Batman: Arkham Knight — 90%
        136 to 75,   // Mass Effect Legendary Edition — 75%
        137 to 90,   // BioShock Remastered — 90%
        138 to 90,   // Star Wars Jedi: Fallen Order — 90%
        139 to 90,   // Titanfall 2 — 90%
        140 to 60    // Warhammer 40K: Boltgun — 60%
    )

    /**
     * Historical lowest discount percentages for DLCs from SteamDB.
     * Only for DLCs with a Steam App ID. Others get 0.
     */
    private val dlcSteamDbHistoricalDiscount = mapOf(
        1001 to 40,  // Shadow of the Erdtree — 40%
        1002 to 50,  // Phantom Liberty — 50%
        1003 to 90,  // Blood and Wine — 90%
        1004 to 90,  // Hearts of Stone — 90%
        1005 to 80,  // The Frozen Wilds — 80%
        1006 to 25,  // Vessel of Hatred — 25%
        1007 to 50,  // Burning Shores — 50%
        1008 to 50,  // Sunbreak — 50%
        1009 to 80,  // Iceborne — 80%
        1010 to 85,  // The Ringed City — 85%
        1011 to 85,  // Ashes of Ariandel — 85%
        // 1012 = Night of the Raven (Alan Wake 2 DLC) — Epic exclusive, no Steam data
        1013 to 50,  // Echoes of the Eye — 50%
        1014 to 80,  // The Foundation (Control) — 80%
        1015 to 80,  // AWE (Control) — 80%
        1016 to 75,  // Tiny Tina's Assault (Divinity) — 75%
        1017 to 70,  // Seven Deadly Sins (Hitman) — 70%
        1018 to 80,  // The Ancient Gods Part 1 — 80%
        1019 to 80,  // The Ancient Gods Part 2 — 80%
        1020 to 60   // Winters' Expansion (RE Village) — 60%
    )

    /**
     * Game ratings from Metacritic (critic score / 10).
     * Source: metacritic.com — PC version scores.
     * Where no PC Metacritic score exists, the console score is used.
     */
    private val gameRatings = mapOf(
        // ── AAA / Big releases ──
        1 to 9.4,    // Elden Ring — Metacritic 96 (PC adjusted)
        2 to 9.6,    // Baldur's Gate 3 — Metacritic 96
        3 to 9.2,    // The Witcher 3: Wild Hunt — Metacritic 92
        4 to 8.6,    // Cyberpunk 2077 — Metacritic 86 (post-patches)
        5 to 9.3,    // Red Dead Redemption 2 — Metacritic 93
        6 to 9.4,    // God of War Ragnarök — Metacritic 94
        7 to 7.5,    // Starfield — Metacritic 75
        8 to 7.3,    // Diablo IV — Metacritic 73 (PC)
        9 to 8.9,    // Alan Wake 2 — Metacritic 89
        10 to 8.4,   // Hogwarts Legacy — Metacritic 84
        11 to 9.3,   // Resident Evil 4 Remake — Metacritic 93
        12 to 8.6,   // Death Stranding — Metacritic 86 (Director's Cut PC)
        13 to 9.2,   // Street Fighter 6 — Metacritic 92
        14 to 9.0,   // Tekken 8 — Metacritic 90
        15 to 8.5,   // Ghost of Tsushima — Metacritic 85 (PC)
        16 to 8.7,   // The Last of Us Part I — Metacritic 87 (PC post-fix)
        17 to 8.7,   // Spider-Man Remastered — Metacritic 87
        18 to 8.9,   // Horizon Zero Dawn — Metacritic 89
        19 to 8.9,   // Dead Space Remake — Metacritic 89
        20 to 9.0,   // Like a Dragon: Infinite Wealth — Metacritic 90
        21 to 9.2,   // Final Fantasy VII Rebirth — Metacritic 92
        22 to 9.7,   // Persona 5 Royal — Metacritic 97
        23 to 9.4,   // Metaphor: ReFantazio — Metacritic 94
        24 to 7.2,   // Palworld — Metacritic 72
        25 to 8.2,   // Black Myth: Wukong — Metacritic 82
        26 to 7.1,   // S.T.A.L.K.E.R. 2 — Metacritic 71
        27 to 7.6,   // Dragon's Dogma 2 — Metacritic 76 (PC)
        28 to 9.0,   // Sekiro: Shadows Die Twice — Metacritic 90
        29 to 8.5,   // Indiana Jones and the Great Circle — Metacritic 85
        30 to 8.0,   // Senua's Saga: Hellblade II — Metacritic 80
        31 to 9.0,   // Monster Hunter: World — Metacritic 90
        32 to 8.9,   // Dark Souls III — Metacritic 89
        33 to 8.6,   // Armored Core VI — Metacritic 86
        34 to 8.5,   // Jedi: Survivor — Metacritic 85
        35 to 7.3,   // EA Sports FC 25 — Metacritic 73
        36 to 6.8,   // Madden NFL 25 — Metacritic 68
        37 to 6.5,   // NBA 2K25 — Metacritic 65
        38 to 8.4,   // F1 24 — Metacritic 84
        39 to 7.7,   // WWE 2K24 — Metacritic 77
        40 to 7.2,   // The Crew Motorfest — Metacritic 72
        41 to 7.3,   // Need for Speed Unbound — Metacritic 73
        42 to 6.6,   // Forza Motorsport — Metacritic 66
        // ── Horror / Survival ──
        43 to 7.5,   // Ghostwire: Tokyo — Metacritic 75
        44 to 8.5,   // Control Ultimate Edition — Metacritic 85
        45 to 6.8,   // The Callisto Protocol — Metacritic 68
        46 to 8.5,   // Lies of P — Metacritic 85
        47 to 7.9,   // Remnant II — Metacritic 79
        48 to 8.4,   // Resident Evil Village — Metacritic 84
        49 to 7.7,   // Outlast Trials — Metacritic 77
        50 to 7.2,   // Phasmophobia — Metacritic 72 (Early Access)
        51 to 8.4,   // Lethal Company — Metacritic 84 (Early Access)
        52 to 8.3,   // The Forest — Metacritic 83
        53 to 8.7,   // Subnautica — Metacritic 87
        54 to 7.7,   // No Man's Sky — Metacritic 77 (post-updates)
        55 to 8.5,   // Deep Rock Galactic — Metacritic 85
        // ── Simulation / Sandbox ──
        56 to 8.0,   // Elite Dangerous — Metacritic 80
        57 to 8.8,   // Kerbal Space Program — Metacritic 88
        58 to 7.3,   // Astroneer — Metacritic 73
        59 to 8.8,   // Satisfactory — Metacritic 88
        60 to 9.3,   // Factorio — Metacritic 93
        61 to 8.3,   // Terraria — Metacritic 83
        62 to 8.0,   // Valheim — Metacritic 80 (Early Access)
        63 to 7.7,   // Grounded — Metacritic 77
        64 to 6.9,   // Rust — Metacritic 69
        65 to 6.6,   // DayZ — Metacritic 66
        66 to 7.0,   // Ark: Survival Evolved — Metacritic 70
        67 to 6.7,   // Conan Exiles — Metacritic 67
        68 to 7.8,   // Raft — Metacritic 78
        // ── Indie / Adventure ──
        69 to 8.3,   // Stray — Metacritic 83
        70 to 8.3,   // Kena: Bridge of Spirits — Metacritic 83
        71 to 8.8,   // It Takes Two — Metacritic 88
        72 to 7.8,   // A Way Out — Metacritic 78
        73 to 7.8,   // Unravel Two — Metacritic 78
        74 to 9.5,   // Portal 2 — Metacritic 95
        75 to 8.6,   // The Talos Principle 2 — Metacritic 86
        76 to 9.0,   // Tetris Effect: Connected — Metacritic 90
        77 to 9.4,   // Celeste — Metacritic 94
        78 to 9.0,   // Hollow Knight — Metacritic 90
        79 to 9.3,   // Hades — Metacritic 93
        80 to 8.9,   // Dead Cells — Metacritic 89
        81 to 8.5,   // Risk of Rain 2 — Metacritic 85
        82 to 7.9,   // Gunfire Reborn — Metacritic 79
        83 to 8.9,   // Slay the Spire — Metacritic 89
        84 to 8.5,   // Monster Train — Metacritic 85
        85 to 8.6,   // Inscryption — Metacritic 86
        86 to 9.0,   // Dave the Diver — Metacritic 90
        87 to 8.9,   // Stardew Valley — Metacritic 89
        88 to 8.4,   // Unpacking — Metacritic 84
        89 to 8.4,   // Spiritfarer — Metacritic 84
        90 to 8.5,   // Outer Wilds — Metacritic 85
        91 to 8.8,   // What Remains of Edith Finch — Metacritic 88
        92 to 8.1,   // Firewatch — Metacritic 81
        93 to 9.2,   // The Stanley Parable: Ultra Deluxe — Metacritic 92
        94 to 8.7,   // The Witness — Metacritic 87
        95 to 8.9,   // Return of the Obra Dinn — Metacritic 89
        96 to 9.7,   // Disco Elysium: The Final Cut — Metacritic 97
        97 to 9.5,   // Divinity: Original Sin 2 — Metacritic 95
        98 to 8.8,   // Pillars of Eternity II — Metacritic 88
        99 to 8.2,   // Hitman: World of Assassination — Metacritic 82
        100 to 8.8,  // Doom Eternal — Metacritic 88
        // ── F2P games ──
        101 to 7.8,  // League of Legends — Metacritic 78
        102 to 7.9,  // Valorant — Metacritic 79
        103 to 8.1,  // Fortnite — Metacritic 81
        104 to 7.3,  // Marvel Rivals — Metacritic 73
        105 to 9.0,  // Dota 2 — Metacritic 90
        106 to 8.8,  // Apex Legends — Metacritic 88
        107 to 8.4,  // Genshin Impact — Metacritic 84
        108 to 8.3,  // Counter-Strike 2 — Metacritic 83
        109 to 7.2,  // Warframe — Metacritic 72
        110 to 8.6,  // Path of Exile — Metacritic 86
        111 to 8.6,  // Rocket League — Metacritic 86
        112 to 8.0,  // Fall Guys — Metacritic 80
        113 to 7.6,  // Teamfight Tactics — Metacritic 76
        114 to 8.6,  // Honkai: Star Rail — Metacritic 86
        // ── 2024-2025 new releases ──
        115 to 9.0,  // Monster Hunter Wilds — Metacritic 90
        116 to 9.2,  // Clair Obscur: Expedition 33 — Metacritic 92
        117 to 8.8,  // Kingdom Come: Deliverance 2 — Metacritic 88
        118 to 8.2,  // Warhammer 40K: Space Marine 2 — Metacritic 82
        119 to 8.8,  // Silent Hill 2 Remake — Metacritic 88
        120 to 7.4,  // Dragon Age: The Veilguard — Metacritic 74
        121 to 7.8,  // Avowed — Metacritic 78
        122 to 9.0,  // The Elder Scrolls IV: Oblivion Remastered — Metacritic 90
        123 to 8.5,  // Path of Exile 2 (EA) — Metacritic 85
        124 to 8.8,  // Helldivers 2 — Metacritic 88
        125 to 8.7,  // Doom: The Dark Ages — Metacritic 87
        126 to 8.3,  // Age of Mythology: Retold — Metacritic 83
        127 to 8.5,  // Like a Dragon: Pirate Yakuza in Hawaii — Metacritic 85
        128 to 7.3,  // Civilization VII — Metacritic 73
        129 to 8.0,  // Assassin's Creed Shadows — Metacritic 80
        130 to 9.0,  // Balatro — Metacritic 90
        131 to 9.3,  // Hades II (EA) — Metacritic 93
        // ── Classics ──
        132 to 9.7,  // Half-Life: Alyx — Metacritic 93 (PC VR)
        133 to 8.5,  // Prey (2017) — Metacritic 85
        134 to 8.6,  // Dishonored 2 — Metacritic 86
        135 to 8.7,  // Batman: Arkham Knight — Metacritic 87
        136 to 9.7,  // Mass Effect Legendary Edition — Metacritic 97
        137 to 9.5,  // BioShock Remastered — Metacritic 97
        138 to 8.3,  // Star Wars Jedi: Fallen Order — Metacritic 83
        139 to 8.9,  // Titanfall 2 — Metacritic 89
        140 to 8.1   // Warhammer 40K: Boltgun — Metacritic 81
    )

    /**
     * Real PC release dates (YYYY-MM-DD format).
     * Source: Steam store pages, Epic store, official announcements.
     * For multi-platform releases, uses the PC launch date.
     */
    private val releaseDates = mapOf(
        // ── AAA / Big releases ──
        1 to "2022-02-25",    // Elden Ring
        2 to "2023-08-03",    // Baldur's Gate 3
        3 to "2015-05-19",    // The Witcher 3: Wild Hunt
        4 to "2020-12-10",    // Cyberpunk 2077
        5 to "2019-12-05",    // Red Dead Redemption 2 (PC)
        6 to "2024-09-19",    // God of War Ragnarök (PC)
        7 to "2023-09-06",    // Starfield
        8 to "2023-06-06",    // Diablo IV (PC)
        9 to "2023-10-27",    // Alan Wake 2
        10 to "2023-02-10",   // Hogwarts Legacy
        11 to "2023-03-24",   // Resident Evil 4 Remake
        12 to "2019-11-08",   // Death Stranding (PC)
        13 to "2023-06-02",   // Street Fighter 6
        14 to "2024-01-26",   // Tekken 8
        15 to "2024-05-16",   // Ghost of Tsushima (PC)
        16 to "2023-03-28",   // The Last of Us Part I (PC)
        17 to "2022-08-12",   // Spider-Man Remastered (PC)
        18 to "2020-08-07",   // Horizon Zero Dawn (PC)
        19 to "2023-01-27",   // Dead Space Remake
        20 to "2024-01-26",   // Like a Dragon: Infinite Wealth
        21 to "2024-12-12",   // Final Fantasy VII Rebirth (PC)
        22 to "2022-10-21",   // Persona 5 Royal (PC)
        23 to "2024-10-11",   // Metaphor: ReFantazio
        24 to "2024-01-19",   // Palworld (Early Access)
        25 to "2024-08-20",   // Black Myth: Wukong
        26 to "2024-11-20",   // S.T.A.L.K.E.R. 2
        27 to "2024-03-22",   // Dragon's Dogma 2
        28 to "2019-03-22",   // Sekiro: Shadows Die Twice
        29 to "2024-12-09",   // Indiana Jones and the Great Circle
        30 to "2024-05-21",   // Senua's Saga: Hellblade II
        31 to "2018-08-09",   // Monster Hunter: World (PC)
        32 to "2016-04-12",   // Dark Souls III
        33 to "2023-08-25",   // Armored Core VI
        34 to "2023-04-28",   // Jedi: Survivor
        35 to "2024-09-27",   // EA Sports FC 25
        36 to "2024-08-16",   // Madden NFL 25
        37 to "2024-09-06",   // NBA 2K25
        38 to "2024-05-31",   // F1 24
        39 to "2024-03-08",   // WWE 2K24
        40 to "2023-09-14",   // The Crew Motorfest
        41 to "2022-12-02",   // Need for Speed Unbound
        42 to "2023-10-10",   // Forza Motorsport
        // ── Horror / Survival ──
        43 to "2022-03-25",   // Ghostwire: Tokyo
        44 to "2019-08-27",   // Control
        45 to "2022-12-02",   // The Callisto Protocol
        46 to "2023-09-19",   // Lies of P
        47 to "2023-07-25",   // Remnant II
        48 to "2021-05-07",   // Resident Evil Village
        49 to "2023-05-18",   // Outlast Trials (Early Access)
        50 to "2020-09-18",   // Phasmophobia (Early Access)
        51 to "2023-10-23",   // Lethal Company (Early Access)
        52 to "2018-04-30",   // The Forest
        53 to "2018-01-23",   // Subnautica
        54 to "2016-08-09",   // No Man's Sky
        55 to "2020-05-13",   // Deep Rock Galactic
        // ── Simulation / Sandbox ──
        56 to "2014-12-16",   // Elite Dangerous
        57 to "2015-04-27",   // Kerbal Space Program
        58 to "2016-12-16",   // Astroneer (Early Access)
        59 to "2024-09-10",   // Satisfactory
        60 to "2020-08-14",   // Factorio
        61 to "2011-05-16",   // Terraria
        62 to "2021-02-02",   // Valheim (Early Access)
        63 to "2022-09-27",   // Grounded
        64 to "2018-02-08",   // Rust
        65 to "2018-12-13",   // DayZ
        66 to "2017-08-29",   // Ark: Survival Evolved
        67 to "2018-05-08",   // Conan Exiles
        68 to "2022-06-20",   // Raft
        // ── Indie / Adventure ──
        69 to "2022-07-19",   // Stray
        70 to "2021-09-21",   // Kena: Bridge of Spirits
        71 to "2021-03-26",   // It Takes Two
        72 to "2018-03-23",   // A Way Out
        73 to "2018-06-09",   // Unravel Two
        74 to "2011-04-19",   // Portal 2
        75 to "2023-11-02",   // The Talos Principle 2
        76 to "2018-11-09",   // Tetris Effect
        77 to "2018-01-25",   // Celeste
        78 to "2017-02-24",   // Hollow Knight
        79 to "2020-09-17",   // Hades
        80 to "2018-08-07",   // Dead Cells
        81 to "2020-08-11",   // Risk of Rain 2
        82 to "2020-05-22",   // Gunfire Reborn (Early Access)
        83 to "2019-01-23",   // Slay the Spire
        84 to "2020-05-21",   // Monster Train
        85 to "2021-10-19",   // Inscryption
        86 to "2023-06-28",   // Dave the Diver
        87 to "2016-02-26",   // Stardew Valley
        88 to "2021-11-02",   // Unpacking
        89 to "2020-08-18",   // Spiritfarer
        90 to "2019-05-28",   // Outer Wilds
        91 to "2017-04-25",   // What Remains of Edith Finch
        92 to "2016-02-09",   // Firewatch
        93 to "2022-04-27",   // The Stanley Parable: Ultra Deluxe
        94 to "2016-01-26",   // The Witness
        95 to "2018-10-18",   // Return of the Obra Dinn
        96 to "2019-10-15",   // Disco Elysium
        97 to "2017-09-14",   // Divinity: Original Sin 2
        98 to "2018-05-08",   // Pillars of Eternity II
        99 to "2021-01-20",   // Hitman: World of Assassination
        100 to "2020-03-20",  // Doom Eternal
        // ── F2P games ──
        101 to "2009-10-27",  // League of Legends
        102 to "2020-06-02",  // Valorant
        103 to "2017-07-21",  // Fortnite
        104 to "2024-12-06",  // Marvel Rivals
        105 to "2013-07-09",  // Dota 2
        106 to "2020-11-04",  // Apex Legends (Steam)
        107 to "2020-09-28",  // Genshin Impact
        108 to "2023-09-27",  // Counter-Strike 2
        109 to "2013-03-25",  // Warframe
        110 to "2013-10-23",  // Path of Exile
        111 to "2015-07-07",  // Rocket League
        112 to "2020-08-04",  // Fall Guys
        113 to "2019-06-26",  // Teamfight Tactics
        114 to "2023-04-26",  // Honkai: Star Rail
        // ── 2024-2025 new releases ──
        115 to "2025-02-28",  // Monster Hunter Wilds
        116 to "2025-04-24",  // Clair Obscur: Expedition 33
        117 to "2025-02-04",  // Kingdom Come: Deliverance 2
        118 to "2024-09-09",  // Warhammer 40K: Space Marine 2
        119 to "2024-10-08",  // Silent Hill 2 Remake
        120 to "2024-10-31",  // Dragon Age: The Veilguard
        121 to "2025-02-18",  // Avowed
        122 to "2025-04-22",  // The Elder Scrolls IV: Oblivion Remastered
        123 to "2024-12-06",  // Path of Exile 2 (Early Access)
        124 to "2024-02-08",  // Helldivers 2
        125 to "2025-05-15",  // Doom: The Dark Ages
        126 to "2024-09-04",  // Age of Mythology: Retold
        127 to "2025-02-21",  // Like a Dragon: Pirate Yakuza in Hawaii
        128 to "2025-02-11",  // Civilization VII
        129 to "2025-03-20",  // Assassin's Creed Shadows
        130 to "2024-02-20",  // Balatro
        131 to "2024-05-06",  // Hades II (Early Access)
        // ── Classics ──
        132 to "2020-03-23",  // Half-Life: Alyx
        133 to "2017-05-05",  // Prey (2017)
        134 to "2016-11-11",  // Dishonored 2
        135 to "2015-06-23",  // Batman: Arkham Knight
        136 to "2021-05-14",  // Mass Effect Legendary Edition
        137 to "2016-09-16",  // BioShock Remastered
        138 to "2019-11-15",  // Star Wars Jedi: Fallen Order
        139 to "2016-10-28",  // Titanfall 2
        140 to "2023-05-23"   // Warhammer 40K: Boltgun
    )

    private fun dlcImg(dlcId: Int): String {
        val appId = dlcSteamIds[dlcId] ?: return "https://cdn.akamai.steamstatic.com/steam/apps/1245620/header.jpg"
        return "https://cdn.akamai.steamstatic.com/steam/apps/$appId/header.jpg"
    }

    private fun generateDLCsForGame(gameId: Int): List<DLC> {
        return dlcData.filter { it.gameId == gameId }.map { dlcInfo ->
            DLC(
                id = dlcInfo.id,
                name = dlcInfo.name,
                gameId = gameId,
                imageUrl = dlcImg(dlcInfo.id),
                currentPrices = emptyMap(), // Real DLC prices come from Steam API
                historicalDiscount = dlcSteamDbHistoricalDiscount[dlcInfo.id] ?: 0,
                releaseDate = "2024-${String.format("%02d", (dlcInfo.id % 12) + 1)}-15",
                description = dlcInfo.desc
            )
        }
    }

    fun generateGames(): List<Game> = popularGames.map { info ->
        if (info.isF2P) {
            // F2P games: no prices, just available platforms
            Game(
                id = info.id,
                name = info.name,
                description = info.description,
                releaseDate = releaseDates[info.id] ?: "",
                imageUrl = steamImg(info.id),
                rating = gameRatings[info.id] ?: 7.0,
                currentPrices = emptyMap(),
                historicalDiscount = 0,
                tags = info.tags,
                dlcs = emptyList(),
                availablePlatforms = info.platforms,
                steamAppId = steamAppIds[info.id] ?: 0
            )
        } else {
            // Real prices come from store APIs via PriceRefreshManager and are cached in Room.
            // We never use placeholder/mock prices — currentPrices stays empty until the
            // live API responds.
            val histDiscount = steamDbHistoricalDiscount[info.id] ?: 0
            Game(
                id = info.id,
                name = info.name,
                description = info.description,
                releaseDate = releaseDates[info.id] ?: "",
                imageUrl = steamImg(info.id),
                rating = gameRatings[info.id] ?: 7.0,
                currentPrices = emptyMap(),
                historicalDiscount = histDiscount,
                tags = info.tags,
                dlcs = generateDLCsForGame(info.id),
                availablePlatforms = info.platforms,
                steamAppId = steamAppIds[info.id] ?: 0
            )
        }
    }

    /**
     * Get the Steam App IDs mapping (game catalog ID → Steam App ID).
     * Used by PriceRefreshManager for API lookups.
     */
    /**
     * Get the Ubisoft Store AR URL for a game, if available.
     * Returns null if the game doesn't have a hardcoded Ubisoft URL.
     */
    fun getUbisoftUrl(gameId: Int): String? = ubisoftUrls[gameId]

    /**
     * Get the GOG Store URL for a game, if a verified slug is available.
     * Returns null if the game doesn't have a hardcoded GOG URL (falls back to live API result).
     */
    fun getGogUrl(gameId: Int): String? = gogGameUrls[gameId]

    /** Get the Xbox / Microsoft Store product ID for a game, if hardcoded. */
    fun getXboxProductId(gameId: Int): String? = xboxProductIds[gameId]

    /** Get the localized Xbox title hint for search matching, if available. */
    fun getXboxTitleHint(gameId: Int): String? = xboxTitleHints[gameId]

    /** Get the EA App product page URL for a game, if hardcoded. */
    fun getEaUrl(gameId: Int): String? = eaGameUrls[gameId]

    /**
     * Get the Epic Games Store page URL for a game, if a verified slug is available.
     * Returns null if the game doesn't have a hardcoded Epic slug (falls back to live API result).
     */
    fun getEpicUrl(gameId: Int): String? {
        val slug = epicSlugs[gameId] ?: return null
        return "https://store.epicgames.com/en-US/p/$slug"
    }

    fun getSteamAppIdsByName(): Map<String, Int> {
        return popularGames.mapNotNull { info ->
            val steamId = steamAppIds[info.id] ?: return@mapNotNull null
            info.name to steamId
        }.toMap()
    }

    /**
     * Get a map from game name → Xbox product ID for games with known IDs.
     * Used by PriceRefreshManager to look up Xbox prices directly.
     */
    fun getXboxProductIdsByName(): Map<String, String> {
        return popularGames.mapNotNull { info ->
            val productId = xboxProductIds[info.id] ?: return@mapNotNull null
            info.name to productId
        }.toMap()
    }

    /**
     * Get a map from game name → Xbox title hint (localized title for search).
     */
    fun getXboxTitleHintsByName(): Map<String, String> {
        return popularGames.mapNotNull { info ->
            val hint = xboxTitleHints[info.id] ?: return@mapNotNull null
            info.name to hint
        }.toMap()
    }

    /**
     * Get the mapping from game name → list of available store platforms.
     * Used by PriceRefreshManager to limit API calls to only the stores
     * where each game is actually sold — prevents querying GOG for Steam-only
     * games and Epic-only games from being queried on Battle.net, etc.
     */
    fun getGamePlatformsByName(): Map<String, List<String>> {
        return popularGames.associate { info -> info.name to info.platforms }
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
