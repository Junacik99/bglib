# BGLIB Documentation

*BGLIB* is an Android library for the development of smart assistants
for board games (board games library). It utilise various computer
vision techniques and machine learning algorithms. Packages like OpenCV,
ML Kit and Mediapipe are already integrated.

The library is split into $5$ main packages:

-   classes,

-   demos,

-   fragments,

-   imgproc,

-   previews.

To integrate a library into a project a user need to add the jitpack
repository into `settings.gradle.kts` as follows:

    dependencyResolutionManagement {
        repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
        repositories {
            mavenCentral()
            maven { url = uri("https://jitpack.io") }
        }
    }

Afterwards it is possible to integrate the library inside
`build.gradle.kts`:

    dependencies {
        implementation(libs.junacik99.bglib)
    }

Or refer to <https://jitpack.io/#Junacik99/bglib> for detailed
instructions on how to integrate the *bglib* library into an Android
project.

## Classes 

This package implements various classes that are prepared for developers
to implement in their application or they help with other
functionalities.

### Achievement 

Achievement class designed as in the article from Juho Hamari and Veikko
Eranti: Framework for Designing and Evaluating Game-Based Applications
(2011) [@hamarianderanti2011achievements].

#### Constructor parameters: 

-   `name: String` -- Name of the achievement,

-   `description: String = ` -- Description of the achievement,

-   `imageLocked: Int` -- Image to be displayed when the achievement is
    still locked,

-   `imageUnlocked: Int` -- Image to be displayed when the achievement
    is unlocked,

-   `trigger: () -> Boolean` -- Action to check if the achievement is
    triggered,

-   `requirements: List<() -> Boolean> = emptyList()` -- List of
    requirements of availability or because the achievement requires it,

-   `conditions: List<() -> Boolean> = emptyList()` -- List of
    conditions of the achievement (eg. is player using weapon x?),

-   `multiplier: Int = 1` -- How many times should the trigger be
    triggered in order to unlock the achievement,

-   `reward: () -> Unit = ` -- An award action to be performed when the
    achievement is unlocked.

#### Public variables: 

-   `unclocked: Boolean` -- Indicates whether achievement has been
    unlocked yet (defaulte false),

-   `image: Int` -- Current image to be displayed (default imageLocked),

-   `progress: Int` -- Progress value that is updated every time the
    trigger returns true (default 0).

#### Public methods: 

-   `checkRequirements(): Boolean` -- Checks if requirements have been
    met,

-   `checkConditions(): Boolean` -- Checks if conditions have been met,

-   `checkTrigger(): Boolean` -- Checks requirements, conditions and
    trigger,

-   `unlock()` -- Unlocks the achievement,

-   `lock()` -- Locks the achievement,

-   `check()` -- Check all conditions and requirements of the
    achievement (calls checkTrigger() and updates progress).

### Card 

Parcelable class representing a game card with visual and positional
properties. Additionally implements parcelable text elements from the ML
Kit and `Rect` from OpenCV.

#### Properties 

-   `boundingBox: Rect` -- OpenCV rectangle defining card boundaries,

-   `text: String` -- Card content text (default: empty),

-   `ScreenPosition` -- (x,y) display coordinates,

-   `Dimensions` -- Width/height of card,

-   `GridPosition` -- (row,col) in game grid.

#### Key Features 

-   Android `Parcelable` implementation

-   Position tracking in screen and grid space

-   ML Kit text integration support

#### Example 

    val card = Card(Rect(0,0,100,150), "Ace of Spades")

#### RectParcelable 

A parcalable OpenCV `Rect`.

#### ParcelableTextElement 

A parcalable string representing a word.

#### ParcelableTextLine 

A parcalable line of text.

#### ParcelableTextBlock 

A parcalable block of text (paragraph).

#### ParcelableText 

A parcalable ML Kit `Text` structure.

### Dice 

The Dice class represents a standard playing die with configurable
sides, designed for game-based applications requiring random number
generation.

#### Constructor Parameters 

-   `d: Int = `$6$ -- Number of sides on the dice (default: $6$). Must
    be $\geq 1$.

#### Public Variables 

-   `name: String` -- Read-only property returning the dice notation
    (e.g., \"d6\" for 6-sided dice).

#### Public Methods 

-   `roll(): Int` -- Simulates a dice roll, returning a random integer
    between 1 and `d` (inclusive).

#### Constraints 

-   Throws `IllegalArgumentException` if `d` is less than 1 during
    initialization.

#### Example Usage 

    val d6 = Dice() // Creates a standard 6-sided die
    val rollResult = d6.roll() // Returns random value 1-6

    val d20 = Dice(20) // Creates a 20-sided die

### HandLandmarkHelper 

[Mediapipe's licensed
script](https://github.com/google-ai-edge/mediapipe-samples/blob/main/examples/hand_landmarker/android/app/src/main/java/com/google/mediapipe/examples/handlandmarker/HandLandmarkerHelper.kt).

### ImageSegmenterHelper 

[Mediapipe's licensed
script](https://github.com/google-ai-edge/mediapipe-samples/blob/main/examples/image_segmentation/android/category_mask/app/src/main/java/com/google/mediapipe/examples/imagesegmenter/ImageSegmenterHelper.kt).

### Playable 

The Playable class serves as a base class for playable entities (teams,
players, etc.) in game implementations, providing core functionality for
score management and win/loss conditions. Designed to be extensible for
various game types.

#### Constructor Parameters 

-   `name: String` -- The display name of the playable entity (e.g.,
    \"Player 1\" or \"Red Team\").

#### Public Variables 

-   `id: Int` -- Auto-incremented unique identifier (read-only).

-   `score: Int` -- Current cumulative score (default: $0$).

-   `deltaScore: Int` -- Last score change amount (default: $0$).

#### Function Variables 

-   `isWinner: () -> Boolean` -- Lambda defining win condition (default:
    `{ false }`). Override Example:

        player.isWinner = { player.score >= 100 }

-   `hasLost: () -> Boolean` -- Lambda defining loss condition (default:
    `{ false }`).

#### Public Methods 

-   `updateScore(score: Int)` -- Updates both `score` and `deltaScore`.

-   `resetScore()` -- Resets `score` to $0$.

#### Key Features 

-   **Thread-safe ID generation** via `AtomicInteger` in the companion
    object.

-   **Extensible design** through `open` modifiers for inheritance.

-   **Dynamic conditions** via lambda properties for game-specific
    logic.

#### Example Usage 

    val player = Playable("Alice").apply {
        isWinner = { score >= 50 }
        updateScore(20) // score=20, deltaScore=20
    }

### Player 

The Player class extends the Playable base class to represent individual
game participants with additional gameplay-specific properties and
methods. It implements unique identification, life tracking, and card
management functionality.

Extends `Playable`.

#### Constructor Parameters 

-   `playerName: String = ` -- Optional display name for the player.

#### Public Variables 

-   `id: Int` -- Unique auto-incremented identifier (overrides
    `Playable.id`),

-   `lives: Int` -- Current life count (default: $100$),

-   `moveCount: Int` -- Tracks total moves made (default: $0$),

-   `cards: MutableList<Card>` -- Collection of held game cards.

#### Overridden Properties 

-   `hasLost: () -> Boolean` -- Default loss condition when `lives`
    $\leq 0$.

#### Public Methods 

-   `addCard(card: Card)` -- Adds a card to the player's collection,

-   `removeCard(card: Card)` -- Removes a specific card,

-   `updateMove()` -- Increments `moveCount` (placeholder for extended
    logic).

#### Companion Object 

-   `nextId: AtomicInteger` -- Thread-safe ID generator (starts at $1$).

#### Example Usage 

    val player = Player("Alice").apply {
        lives = 50
        addCard(Card("Ace"))
        updateMove()
    }

### Team 

The Team class extends the Playable base class to represent a group of
players in collaborative or competitive game scenarios. It provides
team-specific score aggregation and player management functionality.

Extends `Playable`.

#### Constructor Parameters 

-   `teamName: String = ` -- Optional display name for the team.

#### Public Variables 

-   `id: Int` -- Unique auto-incremented identifier (overrides
    `Playable.id`),

-   `players: MutableList<Player>` -- Collection of team members.

#### Public Methods 

-   `updateScore()` -- Calculates team score as the sum of all players'
    scores.

#### Companion Object 

-   `nextId: AtomicInteger` -- Thread-safe ID generator (starts at $1$).

#### Example Usage 

    val team = Team("Blue Team").apply {
        players.add(Player("Alice"))
        players.add(Player("Bob"))
        updateScore() // team.score = sum of all players' scores
    }
    val totalScore = team.score // Obtain score

### Timer 

The Timer class provides configurable countdown functionality with
multiple time unit representations (seconds, minutes, hours) and
event-driven callbacks. Designed for game timing mechanics with
second-level precision.

#### Constructors 

-   `Timer(time: Int = 0)` -- Base constructor in seconds,

-   `Timer(minutes: Int, seconds: Int)` -- Minute-second initialization,

-   `Timer(hours: Int, minutes: Int, seconds: Int)` -- Full time
    specification.

#### Public Variables 

-   `time: Int` -- Current remaining time in seconds (mutable),

-   `hasStarted: () -> Boolean` -- Lambda indicating active state
    (read-only).

#### Time Conversion Methods 

-   `getSeconds(): Int` -- Extracts seconds component ($0$-$59$),

-   `getMinutes(): Int` -- Extracts minutes component ($0$-$59$),

-   `getHours(): Int` -- Extracts full hours component.

#### Time Setting Methods 

-   `setTime(minutes: Int, seconds: Int)` -- Updates time in
    minute-second format,

-   `setTime(hours: Int, minutes: Int, seconds: Int)` -- Updates time in
    full format.

#### Control Methods 

-   `start(function: () -> Unit, period: Long = 1000): Timer?` Begins
    countdown with callback execution every `period` milliseconds.
    Automatically stops at zero.

#### Example Usage 

    val gameTimer = Timer(5, 30) // 5 minutes, 30 seconds
        gameTimer.start {
        println("gameTimer.getMinutes():{gameTimer.getSeconds()}")
        if (!gameTimer.hasStarted()) println("TIME'S UP!")
    }

## Demos 

Demo activities to showcase the usage of the library.

### AchievementsActivity 

Activity to showcase the use of `Achievements`. `Player` can obtain 2
achievements based on the score and conditions.

### BangDemo 

Demo activity for *Bang!* card recognition. After focusing camera on the
card, predictions of the model are mapped to card with the highest
probability and its short description is displayed.

### CardBaseActivity 

This activity serves as a base class for activities that require camera
access, using CameraActivity and OpenCV. Such as card detection
activities.

### CodenamesDemoActivity 

Extends `CardBaseActivity`.

Demo activity for binary classification of cards from the game
*Codenames*. Showcases the use of model interpreter, as well as
rectangle detection, OCR, card parcelable and grid alignment.

### DatasetBuilderActivity 

Extends `CardBaseActivity`.

This is an activity that serves as a Dataset Builder. It inherits from
`CardBaseActivity` to initialize camera and OpenCV. On successful launch
of this activity a user will be able to take pictures of cards. Those
will be automatically cropped and resized to the specified *frameSize*.
Images will be saved in the MediaStore in the folder.

This activity is supposed to be used to build a custom dataset of the
specified deck of cards that will be later used in model training.

### DetectedCardsActivity 

An activity to show detected cards from `CodenamesDemoActivity`.

### DetectedKeyActivity 

Shows a detected key from `KeyDetectorActivity`.

**Note**: Not accurate yet.

### DetectedTextBlockActivity 

Shows parcelable text blocks sent to this activity. Utilises
`ParcelableText` class from `classes.Card.kt`.

### DiceRollActivity 

Demo activity to show hand detection. User rolls a virtual dice on hand
movement. Similar to `HandDetectionActivity` with additional
functionalities implementing `Dice` class.

### FirebaseActivity 

This activity showcases the use of the Google ML Kit Text Recognition.
It starts with the initialization of the OpenCV Camera and checks the
permissions. Afterwards, it detects structured text using ML Kit.

### HandDetectionActivity 

A simple demo activity that shows how to obtain and visualize hand
landmarks. Utilises mediapipe `HandLandmarkerHelper` and `CameraX`.

### HelpActivity 

Demo activity that shows how to use the help fragment.

### ImageSegmentationActivity 

Demo activity that showcases the use of different image segmentation
algorithms. K-means, mean shift, and deeplabv3. User is able to select
the appropriate segmentation algorithm, choose its parameters, load the
image to be segmented from the galery and apply the selected
segmentation on it.

### KeyDetectorActivity 

Detects a key from the game codenames. Shows the usability of the image
processing techniques like `detectRectOtsu`, `getBoundingBoxes`, or
`rotateImage`.

### LivesActivity 

Demo activity of two players battle. Instantiate class `Player` and show
the usage of its methods.

### ScoreActivity 

Demo activity of the match between two teams. Instantiate class `Team`
and show the usage of its methods.

### TesseractActivity 

A demo activity for the model Tesseract version $4$ or $5$. Checks for
camera permissions, initialize the tesseract base API, loads the model
and in `onCameraFrame` method runs the inference for OCR.

### TimerActivity 

A simple `ComponentActivity` that implements the class `Timer` with
hard-coded time.

## Fragments 

Fragments that can be used as an overlay in activities.

### HelpFragment 

A fragment to display `helpContent` over `CameraX`. `helpContent` is a
Composable.

## Imgproc 

Various image processing functions. Among others, it implements OCR,
card detection, model interpreter, and image segmentation. It is
organised into four files: `CardDetection.kt`, `ImageProcessing.kt`,
`TextDetection.kt` and `Utils.kt`.

### Card Detection 

The file `CardDetection.kt` contains classes and functions for card
detection.

#### Model Interpreter 

Initialise the model with `modelName` and input data shape of
`inputSize`. The value ` numberOfOutputs` describes the size of the
output array. Has functions `preprocessMat` and `predict`. The first one
takes the `Mat` from OpenCV as an argument and returns `FloatArray`. The
array can then be used with the latter function to run inference and
obtain predictions.

#### drawRectangle 

A function `drawRectangle` takes a frame (`Mat`) and a rectangle
(`MatOfPoint2f`) as arguments and draws lines on the frame with a
specified colour.

#### getBoundingBoxes 

A function `getBoundingBoxes` takes a frame (`Mat`) and multiple
rectangles\
(`MutableList<MatOfPoint2f>`) as arguments, obtains bounding boxes for
every rectangle, and calls `drawRectangle` for each bounding box.

Returns: `MutableList<Rect>`.

#### grayGauss 

Preprocess the input image for edge detection. A function `grayGauss`
converts the input image (`Mat`) into gray-scale and applies Gaussian
blur to remove noise.

Returns: `Mat`.

#### detectRectCanny 

Detect rectangle using Canny edge detection. A function
`detectRectCanny` preprocess the frame with `grayGauss` first and then
applies Canny edge detection. Afterwards, if specified, bounding boxes
are obtained and detected rectangles are returned\
as `MutableList<MatOfPoint2f>`.

Returns: `MutableList<MatOfPoint2f>`.

#### detectRectOtsu 

Detect rectangle using Otsu thresholding. A function `detectRectOtsu`
preprocess the frame with `grayGauss` first and then applies Otsu's
thresholding to segment the cards and find contours. Afterwards, if
specified, bounding boxes are obtained and detected rectangles are
returned as `MutableList<MatOfPoint2f>`.

Returns: `MutableList<MatOfPoint2f>`.

### Image Processing 

Specialized image processing functions like colour manipulation, image
transformation of image segmentation are in the source file
`ImageProcessing.kt`.

#### Pixel 

Data class `Pixel` stores $RGB$ values.

#### Vector2i 

Data class `Vector2i` stores vector $2$ of integers.

#### Feature 

`Feature` data class with 5 attributes -- RGB and x, y coordinates.

#### SegmentedImage 

Data class for segmented image.

#### BoundingBox 

Data class for bounding boxes of objects from the `SegmentedImage`. The
label is a `category`.

#### medianFilter 

Applies median filter on the image for denoising.

Returns: `Mat`.

#### gaussFilter 

Applies Gaussian filter on the image for denoising.

Returns: `Mat`.

#### sharpenConv2d 

Applies 2D convolution filter on image to sharpen edges.

Returns: `Mat`.

#### getPixelColor 

Retrieves the colour of the pixel at the specified coordinates.
`ColorInt` -- $RGB$ can be easily represented as `Int` (for example blue
$= 0x0000ff$).

Returns: `Int`.

#### getAvgColor 

Get average colour of the frame/subframe (roi).

. Returns: `Int`.

#### divideFrameIntoGrid 

Divides the input frame into the proportional grid of subframes.

Returns: `List<Mat>`.

#### getColorDistance 

Calculate the euclidean distance (`Int`) between two colours.

Returns: `Int`.

#### getClosestColor 

Calculates the closest colour from the list of colours to the input
colour.

Returns: `Int`.

#### cards2grid 

Sorts unordered list of cards into a uniform grid structure. The
pseudocode of the algorithm is as follows:

1.  Sort cards by their $y$ coordinate -- this should put cards in the
    same row next to each other,

2.  Iterate over the desired number of rows,

3.  Get next $n$ cards from the list,

4.  Sort those cards by their $x$ coordinate,

5.  Assign them grid coordinates,

6.  Proceed to the next row and go to step $3$.

Returns: `List<Card>`.

#### rotateImage 

Rotates the input frame by specified degrees.

Returns: `Mat`.

#### segment_kmeans 

K-Means image segmentation based on five features $RGB$ values and $x$,
$y$ coordinates.

Returns: `SegmentedImage`.

#### scottsRule 

Function for selecting the right window size fro mean-shift. Considers
$n$ number of points (pixels) and $d$ number of dimensions.

Returns: `Int`.

#### silvermansRule 

Function for selecting the right window size fro mean-shift. Considers
$n$ number of points (pixels) and $d$ number of dimensions.

Returns: `Int`.

#### gaussianKernel 

Compute gaussian kernel of the window. Used in `segment_meanshift`.

Returns: `Double`.

#### segment_meanshift 

Mean Shift image segmentation.

Returns: `SegmentedImage`.

#### segment_deeplab 

Image segmentation using deeplabV3 and mediapipe.

Returns: `ByteArray`.

#### createBoundingBox 

Creates a bounding box in the input image for one class.

Returns: `BoundingBox`.

### Text Detection 

Methods used for OCR stored in the file `TextDetection.kt`.

#### getRotationCompensation 

Obtain camera rotation compensation for ML Kit detection. The code is
available at [ML Kit's Android text recognition
page](https://developers.google.com/ml-kit/vision/text-recognition/v2/android#2_prepare_the_input_image).
This step is necessary for ML Kit vision tasks.

Returns: `Int`.

#### detectTextMLKit 

Uses ML Kit to obtain structured text from the input frame.

Results are available in the `onResult` callback.

#### detectTextSuspend 

Suspend function wrapper to text detection using `detectTextMLKit`. When
synchronous calls are needed.

Returns `String`.

#### initTess 

Initialises Tesseract model and returns base API.

Returns: `TessBaseAPI`.

#### detectTextTess 

Text detection using the initialised Tesseract model.

Results are available in the `onResult` callback.

### Utilities 

Various utility functions from `Utils.kt` are:

-   Camera permission,

-   Save image to MediaStore,

-   Convert `Mat` to `Bitmap` (and vice versa),

-   Save frame(s),

-   Resize frames,

-   Calculate rotation angle,

-   Convert `MatOfPoint2f` to `Mat`,

-   Multiply `Point` by `Mat`,

-   Get largest rectangle.

#### checkCamPermission 

Checks if the camera permission is granted.

Returns: `Boolean`.

#### saveImageToMediaStore 

Saves the image to MediaStore. Does not require permission.

Returns: `Uri?`.

#### mat2bitmap 

Converts `Mat` to `Bitmap`.

Returns: `Bitmap`.

#### bitmap2mat 

Converts `Bitmap` to `Mat`.

Returns: `Mat`.

#### saveFrame 

Saves the input frame using `saveImageToMediaStore`.

#### saveFrames 

Saves multiple input frames using `saveImageToMediaStore`.

#### resizeFrames 

Resizes multiple input frames into desired shape.

Returns: `MutableList<Mat>`.

#### getLargestRect 

Obtains the largest rectangle from the input list.

Returns: `Rext?`

#### getSmallestRect 

Obtains the smallest rectangle from the input list.

Returns: `Rext?`

#### getRotationAngle 

Calculates rotation angle in degrees of the input rectangle relative to
the frame.

Returns: `Double`.

#### matOfPoint2ftoMat 

Converts `MatOfPoint2f` to `Mat`.

Returns: `Mat`.

#### mulPointbyMat 

Matrix-point multiplication.

Returns: Point.

## Previews 

Preview models.

### CameraPreview 

Camera Preview for CameraX.
