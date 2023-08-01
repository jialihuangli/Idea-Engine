# Idea-Engine

Something I've been working on as a replacement for Quizlet. Quizlet doesn't have many of the features I want from a study tool, so I decided to apply some of the Java I've learned to make something that fits my needs directly. This project is still a work in progress and this is just the code for it, not the final build.

The main code can be found in /src/sample.



## Feature 1: Decks, Tags, and Subtags

One problem I had was that flashcards were only able to be used within it's own deck, without allowing you to feature it elsewhere. This also made it very hard to manage when you have decks of larger sizes. Especially when learning languages, it helps to separate things into much smaller categories, and allow you to focus on small subsections at a time before adding it to a much larger stack to study long term.

So what I did was create a tagging feature for every card you create, and you can create decks from those tags. I also split tags into subcategorizes called subtags, which gives further control of how I decide to organize my flashcards.



## Feature 2: A smarter way to study

I created this before Quizlet had added the feature of a "know" and "don't know" pile and only had their star system, but my intention is the same as theirs. You should receive less of the flashcards you already know, and be given more of the ones you don't. 

However, I took this a step future. Instead of a binary system, I wanted one that took into consideration how many times you've remembered it consecutively, how many times you've studied it, and how long ago since you last studied the card. So I created a rating system (0-10) that changed based on how many times you got the card correct. This, along with other metadata, pushes cards with the lowest rating to the front, while also refreshing your memory of some cards that you might've forgetten over time. With this system in place, you should be able to work on what you have the most trouble with, while also removing a lot of time wasted rehashing what you already know. 



## Feature 3: The details are important

Quizlet categorizes things into term and definition, and then formats everything as such. But there is a broad array of different types of flashcards, and having the ability to format the text also makes absorbing large pieces of information less tiresome.

When I create flashcards for Chinese, I need to have the character, the pronunction, and the definition. When I want to learn how to read, the character is on the front, while the pronunction and definition are on the back. When I want to practice my ability to recall words in chinese, I prefer to have the definition on the front, while the pronunciation and characters are on the back. This system isn't possible when there's only two categories. This is why I separated flashcards to include three pieces of information. In language cards, you can have the term, pronunciation, and definition as separate entities. In more general cards, they are separated into the term, the main idea, and the details, which I will elaborate on next. 

Some flashcards can be dense with information, so having a place specifically for detailed text that doesn't detract from the main headline I thought would be helpful. But having a block of text makes it hard to skim, and can be daunting to go through. So I added HTML formatting to part of the flashcard to make it easier to change font size, highlight text, create bulletpoints and add weight to informationally loaded flashcards. 



## Feature 4: Data

While studying, I often ask myself a lot of questions, such as: 

"How long do I need to study"
"How much progress have I made?"
"What parts of the deck am I struggling with?"

So using the metadata for each card, tag, and deck, it allows me to know how long it usually takes for me to struggle with recollection. I can also see charts to show my progress over time, and various distributions of my cards. 

The foundation is there, some charts are already implemented into the program, there's just a lot more work to be done in order to provide the best experience. 



## Plans for the future

A lot of what I have is incomplete. Some of the formatting is just placeholders or acts as a proof of concept. Other things I've added the foundation for, but haven't necessary implemented into my code as of yet. 

One thing I will work on after most of everything else is polished is a text-to-speech feature. Although I've had a look into the code and API options for it, it hasn't been something urgently needed to be addressed, especially when every new feature adds 10 other bugs that makes the program non-functional. 

Another thing on the backburner is an AI assisted learning function. The name Idea Engine was conceptialized from the idea that memory functions best when a piece of information is tied to many other pieces of information. This was something I learned from "Moonwalking with Einstein", a book depicting someone's journey to win a memory competition. Many of the world's top memory experts attributed numbers to certain physical objects because they simply have more attributes to work with. Rather than remembering a sequence of numbers, they instead remember that they walked their cat in an airplane, while accidentally spilling a bottle of cooking oil on their first grade teacher. Often, the less mundane the situation, the better their ability to recall longer sequences, and the longer the sequence will be in their memory. Because we know the slipperyness of the cooking oil, the crampedness of being in an airplane, and our love for our cats, we don't need to strain our memory, because the story as a whole evokes a number of other parts of our brain.

That aside, I saw a world where I could connect cards together using Word2Vec and some background formula to put different ideas together. Placing them together would allow the user to try and draw some assocation between each, adding another node to our memory web. With the surge of AI such as ChatGPT, that has pivoted into creating something more complex that I haven't fully idealized yet. 
