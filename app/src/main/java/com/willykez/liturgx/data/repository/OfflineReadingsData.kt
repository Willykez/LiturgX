package com.willykez.liturgx.data.repository

import com.willykez.liturgx.model.LiturgicalColor
import com.willykez.liturgx.model.LiturgicalDay
import com.willykez.liturgx.model.LiturgicalSeason
import com.willykez.liturgx.model.ReadingItem
import java.time.LocalDate
import java.time.Month

object OfflineReadingsData {

    fun generateReadingForDate(date: LocalDate): LiturgicalDay {
        val month = date.month
        val dayOfMonth = date.dayOfMonth
        val dayOfWeek = date.dayOfWeek

        // Check for major solemnities & feast days first
        val specialFeast = getSpecialFeast(date)
        if (specialFeast != null) return specialFeast

        // Season determination logic
        val (season, color) = getSeasonAndColor(date)

        val firstReading = getFirstReadingForSeason(date, season)
        val psalm = getPsalmForSeason(date, season)
        val secondReading = if (dayOfWeek.value == 7) getSecondReadingForSunday(date, season) else null
        val gospel = getGospelForDate(date, season)
        val reflection = generateReflectionSnippet(date, season, gospel)

        val dayTitle = if (dayOfWeek.value == 7) {
            "Sunday in ${season.displayName}"
        } else {
            "${dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }} of ${season.displayName}"
        }

        return LiturgicalDay(
            date = date,
            title = dayTitle,
            season = season,
            color = color,
            rank = if (dayOfWeek.value == 7) "Sunday / Lord's Day" else "Weekday",
            firstReading = firstReading,
            responsorialPsalm = psalm,
            secondReading = secondReading,
            gospel = gospel,
            reflection = reflection,
            saintOfTheDay = getSaintForDay(month, dayOfMonth)
        )
    }

    private fun getSeasonAndColor(date: LocalDate): Pair<LiturgicalSeason, LiturgicalColor> {
        val month = date.month
        val day = date.dayOfMonth

        return when {
            month == Month.DECEMBER && day in 1..24 -> Pair(LiturgicalSeason.ADVENT, LiturgicalColor.PURPLE)
            (month == Month.DECEMBER && day >= 25) || (month == Month.JANUARY && day <= 12) -> Pair(LiturgicalSeason.CHRISTMAS, LiturgicalColor.WHITE)
            month == Month.MARCH || (month == Month.APRIL && day < 12) -> Pair(LiturgicalSeason.LENT, LiturgicalColor.PURPLE)
            (month == Month.APRIL && day >= 12) || month == Month.MAY -> Pair(LiturgicalSeason.EASTER, LiturgicalColor.WHITE)
            else -> Pair(LiturgicalSeason.ORDINARY_TIME, LiturgicalColor.GREEN)
        }
    }

    private fun getSpecialFeast(date: LocalDate): LiturgicalDay? {
        val month = date.month
        val day = date.dayOfMonth

        when {
            month == Month.JANUARY && day == 1 -> return LiturgicalDay(
                date = date,
                title = "Solemnity of Mary, Holy Mother of God",
                season = LiturgicalSeason.CHRISTMAS,
                color = LiturgicalColor.WHITE,
                rank = "Solemnity",
                firstReading = ReadingItem("First Reading", "Numbers 6:22-27", "They shall invoke my name upon the Israelites, and I will bless them.", "The LORD said to Moses: 'Speak to Aaron and his sons and say: Thus you shall bless the Israelites. Say to them: The LORD bless you and keep you! The LORD let his face shine upon you and be gracious to you! The LORD look upon you kindly and give you peace!'"),
                responsorialPsalm = ReadingItem("Responsorial Psalm", "Psalm 67:2-3, 5, 6, 8", "", "May God be gracious to us and bless us; may he cause his face to shine upon us.", "May God be gracious to us and bless us; may his face shine upon us. So may your way be known upon earth among all nations your salvation."),
                secondReading = ReadingItem("Second Reading", "Galatians 4:4-7", "God sent his Son, born of a woman.", "Brothers and sisters: When the fullness of time had come, God sent his Son, born of a woman, born under the law, to ransom those under the law, so that we might receive adoption. As proof that you are children, God sent the Spirit of his Son into our hearts, crying out, 'Abba, Father!'"),
                gospel = ReadingItem("Gospel Reading", "Luke 2:16-21", "They found Mary and Joseph and the infant... and after eight days he was named Jesus.", "The shepherds went in haste to Bethlehem and found Mary and Joseph, and the infant lying in the manger. When they saw this, they made known the message that had been told them about this child. All who heard it were amazed by what had been told them by the shepherds. And Mary kept all these things, reflecting on them in her heart."),
                reflection = "Mary meditated deeply on God's mysterious plan. In quiet contemplation today, bring your hopes and anxieties to Mary, trusting in her maternal intercession as Mother of God.",
                saintOfTheDay = "Blessed Virgin Mary",
                holyDayOfObligation = true
            )

            month == Month.AUGUST && day == 15 -> return LiturgicalDay(
                date = date,
                title = "Solemnity of the Assumption of the Blessed Virgin Mary",
                season = LiturgicalSeason.ORDINARY_TIME,
                color = LiturgicalColor.WHITE,
                rank = "Solemnity",
                firstReading = ReadingItem("First Reading", "Revelation 11:19a; 12:1-6a, 10ab", "A woman clothed with the sun, with the moon under her feet.", "God's temple in heaven was opened, and the ark of his covenant could be seen in the temple. A great sign appeared in the sky, a woman clothed with the sun, with the moon under her feet, and on her head a crown of twelve stars."),
                responsorialPsalm = ReadingItem("Responsorial Psalm", "Psalm 45:10, 11, 12, 16", "", "The queen stands at your right hand, arrayed in gold.", "The queen stands at your right hand in gold of Ophir. Hear, O daughter, and see; turn your ear, forget your people and your father's house."),
                secondReading = ReadingItem("Second Reading", "1 Corinthians 15:20-27", "Christ the firstfruits, then, at his coming, those who belong to Christ.", "Brothers and sisters: Christ has been raised from the dead, the firstfruits of those who have fallen asleep. For since death came through a man, the resurrection of the dead came also through a man."),
                gospel = ReadingItem("Gospel Reading", "Luke 1:39-56", "The Almighty has done great things for me; he has raised up the lowly.", "Mary set out and traveled to the hill country in haste to a town of Judah, where she entered the house of Zechariah and greeted Elizabeth. When Elizabeth heard Mary's greeting, the infant leaped in her womb, and Elizabeth, filled with the Holy Spirit, cried out in a loud voice and said: 'Blessed are you among women, and blessed is the fruit of your womb!'"),
                reflection = "The Assumption offers a beacon of hope for humanity. Mary's glorious bodily intake into heaven reveals our ultimate destiny in Christ if we remain faithful.",
                saintOfTheDay = "Assumption of Our Lady",
                holyDayOfObligation = true
            )

            month == Month.DECEMBER && day == 25 -> return LiturgicalDay(
                date = date,
                title = "Solemnity of the Nativity of the Lord (Christmas)",
                season = LiturgicalSeason.CHRISTMAS,
                color = LiturgicalColor.WHITE,
                rank = "Solemnity",
                firstReading = ReadingItem("First Reading", "Isaiah 52:7-10", "All the ends of the earth will behold the salvation of our God.", "How beautiful upon the mountains are the feet of the one bringing good news, announcing peace, bearing good tidings, announcing salvation, saying to Zion, 'Your God is King!'"),
                responsorialPsalm = ReadingItem("Responsorial Psalm", "Psalm 98:1, 2-3, 3-4, 5-6", "", "All the ends of the earth have seen the saving power of God.", "Sing to the LORD a new song, for he has done wondrous deeds; his right hand has won victory for him, his holy arm."),
                secondReading = ReadingItem("Second Reading", "Hebrews 1:1-6", "God has spoken to us through his Son.", "Brothers and sisters: In times past, God spoke in partial and various ways to our ancestors through the prophets; in these last days, he has spoken to us through the Son, whom he made heir of all things and through whom he created the universe."),
                gospel = ReadingItem("Gospel Reading", "John 1:1-18", "The Word became flesh and made his dwelling among us.", "In the beginning was the Word, and the Word was with God, and the Word was God. He was in the beginning with God. All things came to be through him, and without him nothing came to be. What came to be through him was life, and this life was the light of the human race; the light shines in the darkness, and the darkness has not overcome it. And the Word became flesh and dwelt among us."),
                reflection = "Incarnation is God's unconditional declaration of love. God entered human vulnerability so we might share in divine glory. Rejoice in His holy light today!",
                saintOfTheDay = "The Newborn King",
                holyDayOfObligation = true
            )
        }
        return null
    }

    private fun getFirstReadingForSeason(date: LocalDate, season: LiturgicalSeason): ReadingItem {
        val bank = when (season) {
            LiturgicalSeason.LENT -> lentFirstReadings
            LiturgicalSeason.EASTER -> easterFirstReadings
            LiturgicalSeason.ADVENT -> adventFirstReadings
            else -> ordinaryFirstReadings
        }
        return bank[dailyIndex(date, bank.size)]
    }

    private fun getPsalmForSeason(date: LocalDate, season: LiturgicalSeason): ReadingItem {
        return psalmBank[dailyIndex(date, psalmBank.size)]
    }

    /** Stable per-day pseudo-index so the same date always returns the same pick, but nearby days differ. */
    private fun dailyIndex(date: LocalDate, bankSize: Int): Int {
        if (bankSize <= 1) return 0
        return ((date.toEpochDay() % bankSize + bankSize) % bankSize).toInt()
    }

    private val ordinaryFirstReadings = listOf(
        ReadingItem("First Reading", "Ezekiel 34:11-16", "I myself will look after and tend my sheep.",
            "Thus says the Lord GOD: I myself will look after and tend my sheep. As a shepherd tends his flock when he finds himself among his scattered sheep, so will I tend my sheep. I will rescue them from every place where they were scattered when it was cloudy and dark."),
        ReadingItem("First Reading", "1 Kings 19:9a, 11-13a", "Go outside and stand on the mountain before the Lord.",
            "At the mountain of God, Horeb, Elijah came to a cave where he took shelter. Then the LORD said, 'Go outside and stand on the mountain before the LORD; the LORD will be passing by.' A strong and heavy wind was rending the mountains and crushing rocks, but the LORD was not in the wind. After the wind there was an earthquake, but the LORD was not in the earthquake. After the earthquake there was fire, but the LORD was not in the fire. After the fire there was a tiny whispering sound."),
        ReadingItem("First Reading", "Isaiah 55:1-3", "Come to me heedfully, and your soul shall live.",
            "Thus says the LORD: All you who are thirsty, come to the water! You who have no money, come, receive grain and eat; come, without paying and without cost, drink wine and milk! Come to me heedfully, listen, that you may have life. I will renew with you the everlasting covenant, the benefits assured to David."),
        ReadingItem("First Reading", "Wisdom 12:13, 16-19", "You gave your children good ground for hope.",
            "There is no god besides you who have the care of all, that you need show you have not unjustly condemned. For your might is the source of justice; your mastery over all things makes you lenient to all. For you show your might when the perfection of your power is disbelieved, and in those who know you, you rebuke temerity."),
        ReadingItem("First Reading", "Deuteronomy 30:10-14", "The word is very near to you; you have only to carry it out.",
            "Moses said to the people: 'If only you would heed the voice of the LORD, your God, and keep his commandments and statutes... For this command that I enjoin on you today is not too mysterious and remote for you. No, it is something very near to you, already in your mouths and in your hearts; you have only to carry it out.'"),
        ReadingItem("First Reading", "Amos 8:4-7", "Never will I forget a thing they have done.",
            "Hear this, you who trample upon the needy and destroy the poor of the land! 'When will the new moon be over,' you ask, 'that we may sell our grain, and the sabbath, that we may display the wheat?' The LORD has sworn by the pride of Jacob: Never will I forget a thing they have done!"),
        ReadingItem("First Reading", "Sirach 27:30-28:7", "Forgive your neighbor's injustice; then when you pray, your own sins will be forgiven.",
            "Wrath and anger are hateful things, yet the sinner hugs them tight. The vengeful will suffer the LORD's vengeance, for he remembers their sins in detail. Forgive your neighbor's injustice; then when you pray, your own sins will be forgiven."),
        ReadingItem("First Reading", "Jeremiah 20:7-9", "The word of the LORD has brought me derision all the day.",
            "You duped me, O LORD, and I let myself be duped; you were too strong for me, and you triumphed. All the day I am an object of laughter; everyone mocks me. I say to myself, I will not mention him, I will speak in his name no more. But then it becomes like fire burning in my heart, imprisoned in my bones.")
    )

    private val lentFirstReadings = listOf(
        ReadingItem("First Reading", "Isaiah 58:1-9a", "Is this not the fast that I choose: releasing those bound unjustly?",
            "Thus says the Lord GOD: Cry out full-throated and unsparingly; lift up your voice like a trumpet blast; tell my people their transgression, and the house of Jacob their sins. Is not this the fast that I choose: to loosen the bonds of wickedness, to undo the thongs of the yoke, to let the oppressed go free?"),
        ReadingItem("First Reading", "Genesis 9:8-15", "God's covenant with Noah after the flood.",
            "God said to Noah and to his sons with him: 'See, I am now establishing my covenant with you and your descendants after you and with every living creature that was with you.'"),
        ReadingItem("First Reading", "Exodus 20:1-17", "The Ten Commandments, given to Moses on Sinai.",
            "God spoke all these words: I, the LORD, am your God, who brought you out of the land of Egypt, that place of slavery. You shall not have other gods besides me."),
        ReadingItem("First Reading", "Jeremiah 31:31-34", "I will place my law within them, and write it upon their hearts.",
            "The days are coming, says the LORD, when I will make a new covenant with the house of Israel and the house of Judah. I will place my law within them, and write it upon their hearts; I will be their God, and they shall be my people."),
        ReadingItem("First Reading", "2 Chronicles 36:14-17a, 19-23", "The wrath and mercy of the Lord are revealed in the exile and the return.",
            "In those days, all the princes of Judah, the priests, and the people added infidelity to infidelity, practicing all the abominations of the nations. Early and often did the LORD, the God of their fathers, send his messengers to them, for he had compassion on his people.")
    )

    private val easterFirstReadings = listOf(
        ReadingItem("First Reading", "Acts of the Apostles 4:32-35", "The community of believers was of one heart and mind.",
            "The community of believers was of one heart and mind, and no one claimed that any of his possessions was his own, but they had everything in common. With great power the apostles bore witness to the resurrection of the Lord Jesus, and great favor was accorded them all."),
        ReadingItem("First Reading", "Acts of the Apostles 2:14, 22-33", "God freed Jesus from death, because it was impossible for him to be held by its power.",
            "Peter stood up with the Eleven, raised his voice, and proclaimed: 'You who are Israelites, hear these words. This man, delivered up by the set plan and foreknowledge of God, you killed, using lawless men to crucify him. But God raised him up, releasing him from the throes of death, because it was impossible for him to be held by it.'"),
        ReadingItem("First Reading", "Acts of the Apostles 5:27-32", "We are witnesses of these things, as is the Holy Spirit.",
            "When the captain and the court officers had brought the apostles in and made them stand before the Sanhedrin, the high priest questioned them, saying, 'We gave you strict orders did we not, to stop teaching in that name. Yet you have filled Jerusalem with your teaching.'"),
        ReadingItem("First Reading", "Acts of the Apostles 13:14, 43-52", "We now turn to the Gentiles.",
            "Paul and Barnabas continued through Perga and reached Antioch in Pisidia. On the sabbath they entered the synagogue and sat down. Many Jews and worshipers who were converts to Judaism followed Paul and Barnabas, who spoke to them and urged them to remain faithful to the grace of God.")
    )

    private val adventFirstReadings = listOf(
        ReadingItem("First Reading", "Isaiah 2:1-5", "All nations shall stream toward the mountain of the Lord's house.",
            "This is what Isaiah, son of Amoz, saw concerning Judah and Jerusalem: In days to come, the mountain of the LORD's house shall be established as the highest mountain and raised above the hills. All nations shall stream toward it; many peoples shall come and say: 'Come, let us climb the LORD's mountain, to the house of the God of Jacob, that he may instruct us in his ways.'"),
        ReadingItem("First Reading", "Isaiah 11:1-10", "He shall judge the poor with justice.",
            "On that day, a shoot shall sprout from the stump of Jesse, and from his roots a bud shall blossom. The spirit of the LORD shall rest upon him: a spirit of wisdom and of understanding, a spirit of counsel and of strength, a spirit of knowledge and of fear of the LORD."),
        ReadingItem("First Reading", "Isaiah 40:1-5, 9-11", "The glory of the Lord shall be revealed.",
            "Comfort, give comfort to my people, says your God. Speak tenderly to Jerusalem, and proclaim to her that her service is at an end, her guilt is expiated. A voice cries out: In the desert prepare the way of the LORD! Make straight in the wasteland a highway for our God!"),
        ReadingItem("First Reading", "Isaiah 61:1-2a, 10-11", "I rejoice heartily in the Lord.",
            "The spirit of the Lord GOD is upon me, because the LORD has anointed me; he has sent me to bring glad tidings to the poor, to heal the brokenhearted, to proclaim liberty to the captives and release to the prisoners.")
    )

    private val psalmBank = listOf(
        ReadingItem("Responsorial Psalm", "Psalm 23:1-3a, 3b-4, 5, 6", "",
            "The LORD is my shepherd; I shall not want. In verdant pastures he gives me repose; beside restful waters he leads me; he refreshes my soul. He guides me in right paths for his name's sake. Even though I walk in the dark valley, I fear no evil; for you are at my side with your rod and your staff that give me courage.",
            "The Lord is my shepherd; there is nothing I shall want."),
        ReadingItem("Responsorial Psalm", "Psalm 34:2-3, 4-5, 6-7, 8-9", "",
            "I will bless the LORD at all times; his praise shall be ever in my mouth. Let my soul glory in the LORD; the lowly will hear me and be glad. Glorify the LORD with me, let us together extol his name. I sought the LORD, and he answered me and delivered me from all my fears.",
            "Taste and see the goodness of the Lord."),
        ReadingItem("Responsorial Psalm", "Psalm 145:8-9, 10-11, 13-14", "",
            "The LORD is gracious and merciful, slow to anger and of great kindness. The LORD is good to all and compassionate toward all his works. Let all your works give you thanks, O LORD, and let your faithful ones bless you.",
            "The Lord is gracious and merciful, slow to anger, and of great kindness."),
        ReadingItem("Responsorial Psalm", "Psalm 25:4-5, 8-9, 10, 14", "",
            "Your ways, O LORD, make known to me; teach me your paths. Guide me in your truth and teach me, for you are God my savior. Good and upright is the LORD; thus he shows sinners the way. He guides the humble to justice and teaches the humble his way.",
            "Remember your mercies, O Lord."),
        ReadingItem("Responsorial Psalm", "Psalm 63:2, 3-4, 5-6, 8-9", "",
            "O God, you are my God whom I seek; for you my flesh pines and my soul thirsts like the earth, parched, lifeless and without water. Thus have I gazed toward you in the sanctuary to see your power and your glory, for your kindness is a greater good than life.",
            "My soul is thirsting for you, O Lord my God."),
        ReadingItem("Responsorial Psalm", "Psalm 103:1-2, 3-4, 8, 10", "",
            "Bless the LORD, O my soul; and all my being, bless his holy name. Bless the LORD, O my soul, and forget not all his benefits. He pardons all your iniquities, heals all your ills, redeems your life from destruction, crowns you with kindness and compassion.",
            "The Lord is kind and merciful."),
        ReadingItem("Responsorial Psalm", "Psalm 116:12-13, 15-16, 17-18", "",
            "How shall I make a return to the LORD for all the good he has done for me? The cup of salvation I will take up, and I will call upon the name of the LORD. Precious in the eyes of the LORD is the death of his faithful ones.",
            "I will walk in the presence of the Lord, in the land of the living."),
        ReadingItem("Responsorial Psalm", "Psalm 27:1, 4, 13-14", "",
            "The LORD is my light and my salvation; whom should I fear? The LORD is my life's refuge; of whom should I be afraid? One thing I ask of the LORD; this I seek: to dwell in the house of the LORD all the days of my life.",
            "The Lord is my light and my salvation.")
    )

    private fun getSecondReadingForSunday(date: LocalDate, season: LiturgicalSeason): ReadingItem {
        return secondReadingBank[dailyIndex(date, secondReadingBank.size)]
    }

    private fun getGospelForDate(date: LocalDate, season: LiturgicalSeason): ReadingItem {
        val bank = when (season) {
            LiturgicalSeason.LENT -> lentGospels
            LiturgicalSeason.EASTER -> easterGospels
            LiturgicalSeason.ADVENT -> adventGospels
            else -> ordinaryGospels
        }
        // Offset from the other banks' index so First Reading / Psalm / Gospel don't all roll
        // over on the exact same day and feel less like three independent slot machines.
        return bank[dailyIndex(date.plusDays(3), bank.size)]
    }

    private val secondReadingBank = listOf(
        ReadingItem("Second Reading", "Romans 8:28-30", "We know that all things work for good for those who love God.",
            "Brothers and sisters: We know that all things work for good for those who love God, who are called according to his purpose. For those he foreknew he also predestined to be conformed to the image of his Son, so that he might be the firstborn among many brothers and sisters."),
        ReadingItem("Second Reading", "Philippians 4:6-9", "Have no anxiety at all, and the peace of God will guard your hearts.",
            "Brothers and sisters: Have no anxiety at all, but in everything, by prayer and petition, with thanksgiving, make your requests known to God. Then the peace of God that surpasses all understanding will guard your hearts and minds in Christ Jesus."),
        ReadingItem("Second Reading", "Colossians 3:1-4", "Think of what is above, not of what is on earth.",
            "Brothers and sisters: If then you were raised with Christ, seek what is above, where Christ is seated at the right hand of God. Think of what is above, not of what is on earth."),
        ReadingItem("Second Reading", "1 Corinthians 12:4-11", "There are different kinds of spiritual gifts but the same Spirit.",
            "Brothers and sisters: There are different kinds of spiritual gifts but the same Spirit; there are different forms of service but the same Lord; there are different workings but the same God who produces all of them in everyone.")
    )

    private val ordinaryGospels = listOf(
        ReadingItem("Gospel Reading", "Matthew 5:13-16", "You are the light of the world.",
            "Jesus said to his disciples: 'You are the salt of the earth. But if salt loses its taste, with what can it be seasoned? You are the light of the world. A city set on a mountain cannot be hidden. Just so, your light must shine before others, that they may see your good deeds and glorify your heavenly Father.'"),
        ReadingItem("Gospel Reading", "Mark 10:42-45", "Whoever wishes to be great among you will be your servant.",
            "Jesus called his disciples to himself and said to them: 'You know that those who are recognized as rulers over the Gentiles lord it over them. But it shall not be so among you. Rather, whoever wishes to be great among you will be your servant. For the Son of Man did not come to be served but to serve and to give his life as a ransom for many.'"),
        ReadingItem("Gospel Reading", "Luke 12:35-38", "Gird your loins and light your lamps.",
            "Jesus said to his disciples: 'Gird your loins and light your lamps and be like servants who await their master's return from a wedding, ready to open immediately when he comes and knocks. Blessed are those servants whom the master finds vigilant on his arrival.'"),
        ReadingItem("Gospel Reading", "John 15:9-12", "Remain in my love; love one another as I have loved you.",
            "Jesus said to his disciples: 'As the Father loves me, so I also love you. Remain in my love. If you keep my commandments, you will remain in my love. This is my commandment: love one another as I love you.'"),
        ReadingItem("Gospel Reading", "Matthew 13:44-46", "The kingdom of heaven is like a treasure buried in a field.",
            "Jesus said to his disciples: 'The kingdom of heaven is like a treasure buried in a field, which a person finds and hides again, and out of joy goes and sells all that he has and buys that field. Again, the kingdom of heaven is like a merchant searching for fine pearls.'"),
        ReadingItem("Gospel Reading", "Luke 15:1-7", "Rejoice with me because I have found my lost sheep.",
            "Tax collectors and sinners were all drawing near to listen to Jesus, but the Pharisees and scribes began to complain. So Jesus addressed this parable to them: 'What man among you having a hundred sheep and losing one of them would not leave the ninety-nine in the desert and go after the lost one until he finds it?'"),
        ReadingItem("Gospel Reading", "Matthew 6:25-34", "Do not worry about tomorrow.",
            "Jesus said to his disciples: 'Do not worry about your life, what you will eat or drink, or about your body, what you will wear. Look at the birds in the sky; they do not sow or reap, yet your heavenly Father feeds them. Are not you more important than they?'"),
        ReadingItem("Gospel Reading", "John 6:35-40", "I am the bread of life.",
            "Jesus said to the crowds: 'I am the bread of life; whoever comes to me will never hunger, and whoever believes in me will never thirst. For this is the will of my Father, that everyone who sees the Son and believes in him may have eternal life.'")
    )

    private val lentGospels = listOf(
        ReadingItem("Gospel Reading", "Matthew 4:1-11", "Jesus fasted for forty days, and was tempted by the devil.",
            "Jesus was led by the Spirit into the desert to be tempted by the devil. He fasted for forty days and forty nights, and afterwards he was hungry. The tempter approached and said to him, 'If you are the Son of God, command that these stones become loaves of bread.' He said in reply, 'One does not live by bread alone.'"),
        ReadingItem("Gospel Reading", "Luke 9:28b-36", "This is my chosen Son; listen to him.",
            "Jesus took Peter, John, and James and went up the mountain to pray. While he was praying his face changed in appearance and his clothing became dazzling white. A cloud came and cast a shadow over them, and they became frightened when they entered the cloud. Then from the cloud came a voice that said, 'This is my chosen Son; listen to him.'"),
        ReadingItem("Gospel Reading", "John 4:5-42", "Give me this water, that I may not thirst.",
            "Jesus came to a town of Samaria called Sychar, near the plot of land that Jacob had given to his son Joseph. Jacob's well was there. A Samaritan woman came to draw water, and Jesus said to her, 'Give me a drink.' Jesus answered her, 'Everyone who drinks this water will be thirsty again; but whoever drinks the water I shall give will never thirst.'"),
        ReadingItem("Gospel Reading", "Luke 15:1-3, 11-32", "Your brother was dead and has come to life again.",
            "The tax collectors and sinners were all drawing near to listen to Jesus, but the Pharisees and scribes began to complain. So Jesus addressed this parable to them: 'A man had two sons, and the younger son said to his father, Father give me the share of your estate that should come to me.'")
    )

    private val easterGospels = listOf(
        ReadingItem("Gospel Reading", "John 20:19-31", "Peace be with you. As the Father has sent me, so I send you.",
            "On the evening of that first day of the week, when the doors were locked, Jesus came and stood in their midst and said to them, 'Peace be with you.' Thomas, called Didymus, one of the Twelve, was not with them when Jesus came. He said to them, 'Unless I see the mark of the nails in his hands, I will not believe.'"),
        ReadingItem("Gospel Reading", "Luke 24:13-35", "They recognized him in the breaking of the bread.",
            "Two of Jesus' disciples were going to a village called Emmaus, and they were conversing about all the things that had occurred. And it happened that while they were conversing and debating, Jesus himself drew near and walked with them. While he was with them at table, he took bread, said the blessing, broke it, and gave it to them. With that their eyes were opened and they recognized him."),
        ReadingItem("Gospel Reading", "John 10:1-10", "I am the gate for the sheep.",
            "Jesus said: 'Amen, amen, I say to you, whoever does not enter a sheepfold through the gate but climbs over elsewhere is a thief and a robber. I am the gate for the sheep. Whoever enters through me will be saved, and will come in and go out and find pasture.'"),
        ReadingItem("Gospel Reading", "John 15:1-8", "I am the vine, you are the branches.",
            "Jesus said to his disciples: 'I am the true vine, and my Father is the vine grower. I am the vine, you are the branches. Whoever remains in me and I in him will bear much fruit, because without me you can do nothing.'")
    )

    private val adventGospels = listOf(
        ReadingItem("Gospel Reading", "Matthew 24:37-44", "Stay awake! For you do not know on which day your Lord will come.",
            "Jesus said to his disciples: 'As it was in the days of Noah, so it will be at the coming of the Son of Man. Therefore, stay awake! For you do not know on which day your Lord will come.'"),
        ReadingItem("Gospel Reading", "Luke 1:26-38", "Behold, you will conceive in your womb and bear a son.",
            "The angel Gabriel was sent from God to a virgin betrothed to a man named Joseph, of the house of David, and the virgin's name was Mary. And coming to her, he said, 'Hail, favored one! The Lord is with you.' Mary said, 'Behold, I am the handmaid of the Lord. May it be done to me according to your word.'"),
        ReadingItem("Gospel Reading", "Matthew 3:1-12", "Prepare the way of the Lord.",
            "John the Baptist appeared, preaching in the desert of Judea and saying, 'Repent, for the kingdom of heaven is at hand!' It was of him that the prophet Isaiah had spoken when he said: 'A voice of one crying out in the desert, prepare the way of the Lord, make straight his paths.'"),
        ReadingItem("Gospel Reading", "Luke 3:10-18", "He will baptize you with the Holy Spirit and fire.",
            "The crowds asked John the Baptist, 'What should we do?' He said to them in reply, 'Whoever has two cloaks should share with the person who has none. And whoever has food should do likewise.' John answered them all, 'I am baptizing you with water, but one mightier than I is coming. He will baptize you with the Holy Spirit and fire.'")
    )

    private fun generateReflectionSnippet(date: LocalDate, season: LiturgicalSeason, gospel: ReadingItem): String {
        val prompts = listOf(
            "Notice how His message challenges our everyday priorities. Take 5 minutes in quiet prayer to surrender your anxieties to Him.",
            "Ask for the grace to carry this Gospel into one concrete choice you'll make today.",
            "Sit with a single phrase from this passage and let it return to mind throughout the day.",
            "Consider one person you could show this same mercy or attentiveness to today."
        )
        val prompt = prompts[dailyIndex(date, prompts.size)]
        return "Reflecting on ${gospel.citation}: $prompt"
    }


    private fun getSaintForDay(month: Month, day: Int): String {
        return when {
            month == Month.JANUARY && day == 28 -> "St. Thomas Aquinas"
            month == Month.FEBRUARY && day == 14 -> "Sts. Cyril and Methodius"
            month == Month.MARCH && day == 19 -> "St. Joseph, Spouse of the BVM"
            month == Month.APRIL && day == 29 -> "St. Catherine of Siena"
            month == Month.MAY && day == 31 -> "Visitation of the BVM"
            month == Month.JUNE && day == 24 -> "Nativity of St. John the Baptist"
            month == Month.JULY && day == 31 -> "St. Ignatius of Loyola"
            month == Month.AUGUST && day == 28 -> "St. Augustine"
            month == Month.SEPTEMBER && day == 30 -> "St. Jerome"
            month == Month.OCTOBER && day == 4 -> "St. Francis of Assisi"
            month == Month.NOVEMBER && day == 1 -> "All Saints Day"
            month == Month.DECEMBER && day == 12 -> "Our Lady of Guadalupe"
            else -> "Saint of the Day"
        }
    }
}
