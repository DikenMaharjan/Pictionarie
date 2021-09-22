package com.example.pictionarie;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import androidx.annotation.NonNull;

import com.example.pictionarie.model.Player;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Server {
    public static final List<String> WORDS_LIST = Arrays.asList("sponge", "toaster", "telephone", "pillow", "copyright", "fan", "Zebra", "Whistle", "fireworks", "full", "hang glider", "drip", "life", "clue", "squiggle", "Tadpole", "boot", "number", "Wall", "bleach", "commercial", "France", "Needle", "Treasure", "Avocado", "Pinwheel", "Dumbbell", "car accident", "Market", "carat", "cooler", "drain", "half", "rhinoceros", "Motorcycle", "Bonnet", "goblin", "Back", "bed", "coat", "arrows", "campfire", "giant", "Curtain", "ditch", "string bean", "cabin", "fireside", "basin", "Lobster", "Animal", "bottle cap", "PingPong", "tree", "calm", "Bible", "root", "cockpit", "Snowball", "bat", "Frog", "Oyster", "garden", "eyeball", "barn ", "Yard", "elbow", "beard", "curtain", "mailbox", "Scooter", "diving", "hoodies", "Sparrow", "bone", "woman", "button", "demanding", "passport", "Girl", "fog", "dashboard", "Charger", "blush", "Sugar", "mail", "clog", "computer monitor", "net", "decipher", "constrictor", "bubble bath", "fun house", "Fox", "stitches", "Playground", "apathy", "gymnast", "bathtub", "windmill", "radio", "Owl", "Cabin", "Monkey", "Portugal", "brainstorm", "Fire Man", "Pantyhose", "Lipstick", "triangle", "money", "Moth", "Garden", "heroes", "Purse", "Kiwi", "Grass", "Shark", "dress shirt", "Ceiling", "hawaii", "milk", "animal", "hermit crab", "Car", "dust bunny", "balance beam", "fowl", "Tent", "Golden", "Zombie", "clamp", "Bubble", "gondola", "friction", "Evening", "Toothpaste", "bamboo", "hole", "alert", "Attic", "broccoli", "Flagpole", "Sleep", "Eiffel Tower", "full moon", "smiley face", "Hot Tub", "key", "word", "confidant", "education", "wheel", "Nightmare", "Quilt", "stick", "paint can", "sword", "food", "cheerleader", "kettle", "picture", "banister", "Ghost", "dugout", "deodorant", "Bow tie", "cadaver", "great-grandfather", "building", "Furniture", "heart", "elope", "boxing", "Ray", "Dragonfly", "centipede", "Cloak", "fur", "dress", "boa constrictor", "window", "hockey", "stereo", "distraction", "forklift", "girl", "abraham lincoln", "station", "Umbrella", "Vulture", "Giant", "point", "Ox", "crust", "braces", "extension cord", "pen", "extension", "case", "coffee cup", "pants", "Circus", "salesclerk", "Statue of Liberty", "Mom", "cheerleader dust", "chain saw", "Pacifier", "avocado", "Disease", "cable car", "diamond", "Pencil", "golf club", "bulb", "darts", "crew", "zigzag", "coach", "Rainbow", "Handle", "everglades", "Koala", "crop duster", "Money", "Badger", "Deodorant", "Restaurant", "bowling", "Flamingo", "eel", "dryer sheets", "Cat", "credit", "chisel", "light", "socks", "island", "classroom", "discovery", "bride wig", "rope", "dictate", "umbrella", "Iron", "Sushi", "Energy", "fresh water", "cleaning spray", "t-shirt", "motorbike", "diversity", "seed", "stocking", "dust", "first class", "altitude", "cardboard", "ticket", "chevrolet", "toe", "cattle", "popsicle", "crate", "Fountain", "fragment", "Seagull", "tennis raquet", "plane", "Nature", "bug", "hay wagon", "cat", "thumb", "sandwich", "table", "blade", "deep", "Airport", "School", "bus", "Banana", "monkeys", "gravity", "eggs", "Seat", "customer", "Notebook", "syringe", "Deer", "Belgium", "Queen", "Lizard", "Breakfast", "job", "baseball", "Flag", "fence", "Forest", "Artist", "ambulance", "pond", "Van", "Balloon", "creator", "Bicycle", "Scrambled", "biscuit", "Coyote", "Toast", "Frame", "Coral", "adidas", "faucet", "Lighter", "Ladybird", "Eel", "Yak", "shorts", "ocean", "Pillow", "aircraft carrier", "worm", "screw", "Glue", "Battery", "stomach", "Hedgehog", "toothpaste", "Wire", "snail", "Bedbug Hot Tub", "eureka", "bedbug", "mosquito", "Library", "trombone", "chain mail", "blackberry", "goatee", "Eyeball", "cot", "tractor", "Face", "Pirate", "floor lamp", "avalanche", "television", "student", "grenade", "Crow", "Lion", "chicken coop", " Fluffy", "Pigtails", "home", "glasses", "Branch", "Island", "farm", "Hurdle", "Lawyer", "enemy", "convenience store", "cubit", "Hospital", "Sun Burn", "shark", "Starfish", "flamingo", "castaway", "power outlet", "group", "Hook", "hot air balloon", "Cardboard", "coconut", "Crib", "lollipop", "depth", "pig", "dice", "buzz lightyear", "family tree", "danger", "germany", "muscle", "boots", "Nigeria", "Arctic", "Jump", "Train", "bookstore", "frog", "watermelon", "Match", "cloak", "lightning", "spade", "Toad", "nerve", "Strawberry", "mother", "zebra", "horse", "Tricycle", "drive-through", "Safe", "WiFi", "Swamp", "camera", "degree", "Hawk", "hang", "nut", "camel", "pot", "short monster", "hamburger", "Cupcake", "destruction", "Truck", "owl", "octagon", "Ski", "Alligator", "hearse", "Pumpkin", "mouse", "Louse", "Cockroach", "bucket", "necklace", "pole", "coil", "Advertisement", "archaeologist", "camouflage", "urchin", "diversify", "form", "bandage", "Magazine", "scissors", "earmuffs", "Spoon", "homework", "Junk", "Peach", "fake flowers", "bell", "finger", "trumpet", "Raven", "Jelly", "Crowd", "Mouse", "Stairs", "Diamond", "sink", "black hole", "grim reaper", "The Great Wall of China", "drawer", "House", "Greece", "Hamster", "Rain", " aircraft carrier", "Family", "hedgehog", "Doctor", "climate", "Song", "envelope", "Snake", "elf", "fad", "Eye", "gallon", "band", "parrot", "angel", "year", "tablecloth", "Lawnmower", "knot", "Watering Can", "dodge ball", "deliver", "chess", "chain", "atlas", "bee ", "story", "ceiling fan", "Dust", "Rat", "photograph", "exponential", "garden hose", "Fireman", "drift", "flying saucer", "Carpet", "Comedian", "Hair Dryer", "blacksmith", "chariot", "part", "arch", "rain", "bible", "coast", "flavor", "cannon", "food court", "penguin", "Volleyball", "stamp", "cactus", "child", "Slipper", "cello", "detail", "finding nemo", "work", "hat", "door", "drums", "Squid", "alphabet", "airport security", "Goose", "Tourist", "Sprinkler", "teacher", "audi", "laptop", "book", "accordion", "house plant", "Painting", "swing set", "firefighter", "Post Office", "Bumble", "snorkel", "Think", "lion", "Ant", "office", "panda", "lighthouse", "Doormat", "Boat", "fishing", "Ambulance", "Box", "deceive", "cherub", "Diving", "chord", "trousers", "cupcake", "beach", "ceiling", "blunt", "duck", "line", "demon", "Orange", "Mosquito", "traffic light", "Robin", "Tusk", "Cartoon", "skateboard", "donut", "picture frame", "ginger", "steak", "Solar", "Fizz", "crib", "yoga", "cruise ship", "Computer", "donald trump", "Traffic", "drought", "charger", "lighter", "Flower", "daughter in law", "rainbow", "grill", "Worm", "alcohol", "flip flops", "Sunburn", "Mattress", "chick-fil-a", "Birthday", "Turtle", "raincoat", "error", "flowers", "Teacher", "microwave", "Kite", "hot", "High", "card", "England", "glitter", "water", "bunny", "bear", "tail", "blueberry", "pie", "brick", "fizz", "firetruck", "people", "baguette", "Trip", "Piano", "foot", "month", "Bee", "banana", "River", "Tiptoe", "bush", "Answer", "descendant", "pump", "bikini", "earache", "Oar", "Dove", "fireman", "area", "raccoon", "Seahorse", "Refrigerator", "Boy", "Space", "skyscraper", "Gas", "throat", "Death", "harry potter", "compare", "snake", "Shampoo", "cow", "Ferris", "eiffel tower", "branch", "Lunch", "Applause", "hammer", "Kiss", "acre", "dryer sheet", "Birthday Cake", "angle", "shelf", "Skip", "Full", "iPad", "tornado", "Angry", "conveyor belt", "Kitchen", "Doghouse", "pocket", "Hare", "Ocean", "bow tie", "screwdriver", "Pizza", "alligator", "Yacht", "cushion", "right", "brush", "light bulb", "fiddle", "factory", "chaos", "Sandbox", "cheese", "Park", "suitcase", "audience", "Nest", "man", "compromise", "Pigeon", "drowning", "dumbbell", "Buckle", "Seal", "exhibition", "Bat", "Ducks", "waterslide", "Juice", "paper clip", "Fly", "Bed", "download", "detective", "saxophone", "octopus", "cookie", "helmet", "Camera", "bee", "asteroid", "Lock", "Reindeer", "Light Bulb", "peanut", "armada", "collar", "freshwater", "Oxygen", "Abraham Lincoln", "archer", "potato", "Backbone", "computer", "Xylophone", "Sun", "sea turtle", "fork", "needle", "cloud", "hero", "Chime", "problem", "Shrimp", "Vase", "Angel", "fish", "gallop", "Chef", "snowflake", "The Eiffel Tower", "feeling", "lot", "Butterfly", "soccer ball", "Planet", "knife", "Yardstick", "Dress", "century", "atlantis", "toothbrush", "dorsal", "dismantle", "rabbit", "cell", "Raccoon", "bed and breakfast", "front", "Pharmacist", "cream", "Kangaroo", "geologist", "drop", "Tomato", "chameleon", "Crust", "Elephant", "Cow", "vase", "saw", "diagonal", "Leprechaun", "author", "Plank", "daughter", "honk", "cartoonist", "bisexual", "Tiger", "Woodpecker", "river", "belt", "Mount", "Gate", "bench", "eclipse", "Mailman", "seat", "highchair", "basket", "love", "Jellyfish", "mouth", "Machine", "binoculars", "Magnet", "captain america", "Mechanic", "Pineapple", "fun", "America", "Hamburger", "burrito", "graveyard", "carriage", "Kitten", "Football", "air conditioner", "buffalo", "carpenter", "feather", "Season", "china", "system", "berry", "boy", "chemical", "comb", "candle", "default", "newspaper", "cart", "submarine", "team", "barbershop", "economics", "Grasshopper", "bicycle", "Hedgehong", "Mermaid", "car", "fossil", "week", "Head", "chicken nugget", "wine glass", "feeder road", "dead end", "ankle", "Puppet", "flip flips", "onion", "Engine", "Recycle", "thread", "store", "speedboat", "Ostrich", "butterfly", "hair", "disco", "divorce", "kangaroo", "sailboat", "flower", "crab", "clown", "wealth", "Oil", "remote control", "Holiday", "brain", "hoop", "tongue", "crane", "rake", "Speakers", "applause", "compass", "heater", "doubtful", "chime", "pizza", "Pool", "stop sign", "haircut", "knee", "catalog", "parachute", "French Fries", "award", "bottle", "Extension Cord", "snowman", "eraser", "fries", "wristwatch", "glue stick", "Card Board", "Salmon", "thing", "squirrel", "cord", "rifle", "beanstalk", "purse", "ant", "Pelican", "baseball bat", "mermaid", "music", "coffee", "chandelier", "australia", "rail", "Beetle", "gladiator", "family", "King", "orange", "Dragon", "skirt", "Lollipop", "Denmark", "Ping Pong", "scorpion", "night", "State", "Time Machine", "bubble", "Clams", "Wig", "ice cream", "hand", "Beach", "Television", "Rock", "guillotine", "cramp", "Goblin", "cowboy", "Nail", "Sponge", "czar", "map", "Lap", "Egg", "Paper", "clock", "truck", "program", "hand soap", "coworker", "Thief", "bonnet", "Hockey", "grandmother", "Light", "pliers", "ring", "Knight", "firefox", "helicopter", "accounting", "biohazard", "exercise", "giraffe", "captain", "game", "Crayon", "Brother", "cellar", "Olympics", "glue", "Chandelier", "baby", "Kneel", "fire hydrant", "school bus", "Sweater", "Bleach", "couch", "ferris wheel", "guitar", "football player", "Thailand", "Password", "Mushroom", "backpack", "baseboards", "train", "rice", "circus tent", "guru", "goalkeeper", "cone", "Penguin", "Peacock", "Candle", "day", "face", "bride", "Rabbit", "Lady", "paintbrush", "attack", "barney", "Highchair", "rollerskates", "birthday", "state", "Snowflake", "hexagon", "double", "town", "cake", "sun", "exam", "dab", "egg", "Thread", "broom", "jail", "cartography", "Night", "Wax", "Vegetable", "eyeglasses", "Helmet", "dragon", "crow nest", "carpet", "Dinner", "see saw", "channel", "Sneeze", "boat", "fade", "Swan", "ashamed", "dentist", "correct", "boromir", "bobsled", "TV", "cousin", "Tire", "Sand", "earthquake", "Robot", "high tops", "North", "comedian", "crayon", "groot", "match", "arm", "fly", "Whale", "animal migration", "skull", "Rushmore", "baby-sitter", "time", "Caravan", "coronavirus", "disgust", "castle", "champion", "Newspaper", "wire", "piano", "dream works", "Picnic", "flu", "Baseball", "rat", "Joke", "back seat", "funnel", "fabric", "Facebook", "canoe", "cartoon", "balloon", "washing machine", "engine", "handle", "mug", "Otter", "Sandwich", "hail", "flock", "flowchart", "deadpool", "Fog", "axe", "bread", "Soccer", "Teepee", "basketball", "corduroy", "microphone", "bottlecap", "Pocket", "shirt", "explore", "Pig", "glove", "pin", "Leather", "apparatus", "Scale", "attic", "courthouse", "Room", "cd", "foil", "Lamp", "stethoscope", "Glasses", "babies", "Swallow", "bridge", "biceps", "street light", "brunette", "teddy-bear", "Dentist", "father", "Leopard", "bumble bee", "edit", "Raft", "Microphone", "calf", "blueprint", "hopscotch", "spreadsheet", "Cake", "Australia", "cranium", "carnival", "berlin wall", "whistle", "carrot", "dent", "flutter", "Outside", "rod", "Sasquatch", "flat", "toilet", "Honey", "edge", "sail", "wedding dress", "Horse", "engaged", "Chimpanzee", "confide", "Bikini", "Zoo", "keyboard", "Darts", "Plastic", "gavel", "shovel", "bookend", "apple", "Fireworks", "gas station", "Giraffe", "toddler", "The Mona Lisa", "Tutu", "grapes", "movie", "hot tub", "Game", "sheep", "gumball", "Bruise", "Photograph", "Actor", "Black", "stairs", "Spider", "centimetre", "David", "peas", "Shells", "defect", "Retriever", "Hopscotch", "sleeping bag", "Hippopotamus", "equation", "phone", "dishwasher", "Coach", "dumbo", "army", "beluga whale", "Jackal", "beans", "gun", "pencil", "hospital", "moustache", "atmosphere", "hockey puck", "Shoe", "apron", "van", "Shallow", "Goat", "leaf", "gratitude", "cutlass", "Hydrogen", "electrical outlet", "head", "floor", "bitcoin", "hitler", "graduation", "Toothbrush", "doppelganger", "Baker", "cubicle", "Shrink", "Telescope", "alarm clock", "application", "crumbs", "austin powers", "expired", "Dent", "luggage", "Knife", "Rose", "shoe", "spoon", "gentleman", "Uganda", "back flip", "lantern", "Hair", "Insect", "Dream", "hourglass", "whip", "crown", "cup", "United States", "Helicopter", "asparagus", "Popsicle", "nail", "criticize", "swan", "doubt", "Easter", "Napkin", "Window", "Manchester", "blizzard", "Telephone", "crime", "Glass", "Firefighter", "Chicken", "crisp", "nose", "Country", "germ", "Centipede", "diver", "square", "box", "Finland", "Chess", "Garage", "ship", "Bus", "company", "pineapple", "consent", "Captain", "eighteen-wheeler", "brake", "effect", "hang ten", "Goldfish", "tooth", "bowtie", "frame", "Beard", "wing", "Raindrop", "Deep", "mountain", "crocodile", "duvet", "monkey", "Afternoon", "megaphone", "Sheet", "leg", "bulldog", "dance", "house", "eagle", "Pilot", "gold medal", "Bathroom", "boulevard", "Brain", "galaxy", "cats", "angry", "Bucket", "Mole", "handful", "Dance", "Tropical", "Bedbug", "drill", "teenager", "Rug", "skin", "heel", "diving board", "chair", "Stone", "Flying", "Stethoscope", "Photo", "Russia", "Internet", "wolf", "Castle", "hockey stick", "Pajamas", "Ice", "turtle", "headphones", "Cormorant", "alice in wonderland", "Electricity", "snow", "cruise", "Peanut", "Cruise", "community", "hurricane", "minister", "Book", "accident", "Cliff", "advertisement", "Stingray", "downpour", "dryer", "Army", "boomerang", "clique", "Cowboy", "sweater", "Convertible", "Blue", "cape", "fast food", "cliff", "baggage", "Blizzard", "florist", "goat", "ear", "Megaphone", "first mate", "pear", "bruise", "athlete", "Beehive", "Insurance", "tent", "China", "strawberry", "tiger", "gasoline", "Hot Dog", "Stick", "brand", "jacket", "Magician", "art gallery", "Quill", "spider", "bunch", "room", "Garfield", "plate", "flag", "postcard", "drugstore", "boundary", "stove", "speaker", "Lace", "business", "Crab", "violin", "Gold", "Camel", "companion", "Coal", "bag", "marker", "ships", "lock", "chicken legs", "frying pan", "pickup truck", "anvil", "cliff diving", "pipe", "bracelet", "Mascot", "high heel", "Baby", "matches", "sock", "Jewellery", "eye", "Stork", "plough", "fireplace", "frost", "Sweden", "wine bottle", "wall", "roller coaster", "gown", "conversation", "calendar", "church", "cell phone", "moon", "star", "Nurse", "Walrus", "golden retriever", "palm tree", "aunt", "fireman pole", "Teapot", "comfy", "Hotel", "saucer", "check", "elephant", "underwear", "Guitar", "Jordan", "Stomach", "pigtails", "board", "bald", "evolution", "Sheets", "Drip", "darth vader", "dizzy", "buckle", "bird", "Gorilla", "village", "emperor", "lipstick", "binder", "convertible", "Puppy", "Salt and Pepper", "Church", "dripping", "school", "businessperson", "Squirrel", "spring", "harp", "Skate", "oven", "beer pong", "Flute", "Apple", "Octopus", "Suitcase", "disc jockey", "Burrito", "parcel", "Minivan", "roof", "anemone", "daffy duck", "bushel", "hook", "doorbell", "receipt", "world", "bugs bunny", "doubloon", "Golden Retriever", "Knee", "teapot", "flotsam", "ladder", "baker", "apathetic", "eyes", "Chalk", "street", "Cheerleader", "dog", "emigrate", "lobster", "actor", "country", "ball", "Igloo", "pool", "jewel", "hair dryer", "chef", "Garbage", "Bomb", "chimney", "prison", "Flat", "sunlight", "airplane", "flashlight", "groom", "horn", "study", "amusement park", "Dog", "Nose", "Panda", "chin", "county fair", "Rocket", "Turkey", "calculator", "flute", "fuel", "grain", "Bear", "Sachin", "figment", "fish bone", "employee", "Fish", "neck", "mushroom", "government", "Potato", "chariot racing", "gold", "con", "gamer", "Sheep", "emotions", "police car", "heaven", "lip", "Mr", "dolphin", "Houseboat", "fact", "Egypt", "question", "watch", "cricket", "Mailbox", "bluetooth", "whale", "dream", "flight", "guess", "Leak", "stem", "good-bye", "bulldozer", "Sea", "Dolphin", "London", "place", "Salmfor on", "Morning", "tray", "dresser", "comparison", "circle", "border", "Elk", "Spring", "Chain", "Music", "Crocodile", "forrest gump", "Mask", "way", "bath", "geyser", "grandpa", "Eggplant", "Parrot", "hot dog", "biology", "Raincoat", "Daughter", "Lighthouse", "grass", "birthday cake");
     public static final String WORDS_KEY = "Words";

    public static final String SERVER_KEY = "Servers";

    public static final String INFORMATION_KEY = "Information";
    public static final String PLAYER_KEY = "Players";
    public static final String MESSAGE_KEY = "Messages";
    public static final String DRAWING_KEY = "Drawings";
    static String CURRENT_WORD_KEY = "CurrentWord";
    public static final String HINTS_KEY = "Hint";
    public static final String NUMBER_OF_PLAYERS_KEY = "NumberOfPlayers";

    public static final String SEGMENTS_KEY = "Segments";
    public static final String CURRENT_SEGMENT_VALUE = "CurrentSegmentValue";
    public static final String CURRENT_SEGMENT_POINTS = "CurrentPoints";
    public static final String ANSWERS_KEY = "PlayersAnswered";
    public static final String CURRENT_STATE = "CurrentStates";
    public static int numberOfPlayers;
    static int CODE_LENGTH = 6;
    static Player player;
    static String serverCode;
    public static DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    public static List<Player> playerList = new ArrayList<>();
    public static HashMap<String, String> turnPlayerHashmap = new HashMap<>();

   private Server() {

    }

    public static DatabaseReference getCurrentStateReference(){
        return getDatabaseReference().child(CURRENT_STATE);
    }

    public static DatabaseReference getDatabaseReference(){
        return databaseReference.child(SERVER_KEY).child(serverCode);
    }
    public static DatabaseReference getAnswersRef(){
        return getDatabaseReference().child(ANSWERS_KEY);
    }
    public static DatabaseReference getCurrentWordReference(){
        return getDatabaseReference().child(CURRENT_WORD_KEY);
    }
    public static DatabaseReference getGameInformationReference(){
        return getDatabaseReference().child(INFORMATION_KEY);
    }

    public static Player getPlayer() {
        return player;
    }

    public static DatabaseReference getHintReference(){
       return getDatabaseReference().child(HINTS_KEY);
    }

    public static DatabaseReference getMessageRef(){
        return getDatabaseReference().child(MESSAGE_KEY);
    }

    public static DatabaseReference getDrawingRef(){
        return getDatabaseReference().child(DRAWING_KEY);
    }

    public static DatabaseReference getPlayerRef(){
        return getDatabaseReference().child(PLAYER_KEY);
    }

    public static DatabaseReference getNumberOfPlayerRef(){
        return getDatabaseReference().child(NUMBER_OF_PLAYERS_KEY);
    }

    public static void clear(){
        if (player != null){
            player = null;
        }
        if (serverCode != null){
            serverCode = null;
        }
        if (!playerList.isEmpty()){
            playerList.clear();
        }
        numberOfPlayers = 0;
    }






}
