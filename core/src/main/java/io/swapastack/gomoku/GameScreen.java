package io.swapastack.gomoku;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.net.URI;

/**
 * This is the GameScreen class.
 * This class can be used to implement your game logic.
 * <p>
 * The current implementation provides a colorful grid and a leave game button.
 * <p>
 * A good place to gather further information is here:
 * https://github.com/libgdx/libgdx/wiki/Input-handling
 * Input and Event handling is necessary to handle mouse and keyboard input.
 *
 * @author Dennis Jehle & Ibtsam Ali Mahmood
 */
public class GameScreen implements Screen {
    //Gamepiece placing
    private final GameScreenModel gameScreenModel;
    // reference to the parent object
    private final Gomoku parent_;
    // OrthographicCamera
    private final OrthographicCamera camera_;
    // Viewport
    private final Viewport viewport_;
    // Stage
    private final Stage stage_;
    //multiplexer
    private InputMultiplexer multiplexer;
    // SpriteBatch
    private final SpriteBatch sprite_batch_;
    // ShapeRenderer
    // see: https://libgdx.badlogicgames.com/ci/nightlies/docs/api/com/badlogic/gdx/graphics/glutils/ShapeRenderer.html
    private final ShapeRenderer shape_renderer_;
    // Skin
    private final Skin skin_;
    // see: https://libgdx.badlogicgames.com/ci/nightlies/docs/api/com/badlogic/gdx/graphics/Texture.html
    private final Texture background_texture_;
    // Colors
    private static final Color white_ = new Color(1.f, 1.f, 1.f, 1.f);
    private static final Color green_ = new Color(0.f, 100.f, 0.f, 1.f);
    private static final Color blue_ = new Color(0.f, 0.f, 1.f, 1.f);
    private static final Color red_ = new Color(100.f, 0.f, 0.f, 1.f);
    //Player Input in Dialog
    private TextField dialog_player_input1;
    private TextField dialog_player_input2;
    //Player Label
    private Label player_label2;
    //Openingrule
    private int opening_rule;
    //background Music
    private final Music background_music_;
    Player[][] gamestone_positions;
    // grid dimensions
    private static final int grid_size_ = 15;
    private static final float padding = 100.f;
    private static final float line_width = 5.f;


    /**
     * @param parent
     * Alle wichtigen Initialisierungen der globalen Varibalen sind hier.
     * Grafische Dinge wurden ebenso hier aufgerufen; Buttons, Hintergrundbild, SWAP2-Regel & aktuelle
     * Spielernzeige sowie die Hintergrundmusik
     *
     */
    public GameScreen(Gomoku parent) {
        //GamescreemModel method
        gameScreenModel = new GameScreenModel();
        // store reference to parent class
        parent_ = parent;
        // initialize OrthographicCamera with current screen size
        // e.g. OrthographicCamera(1280.f, 720.f)
        Tuple<Integer> client_area_dimensions = parent_.get_window_dimensions();
        camera_ = new OrthographicCamera((float) client_area_dimensions.first, (float) client_area_dimensions.second);
        // initialize ScreenViewport with the OrthographicCamera created above
        viewport_ = new ScreenViewport(camera_);
        // initialize SpriteBatch
        sprite_batch_ = new SpriteBatch();
        // initialize the Stage with the ScreenViewport created above
        stage_ = new Stage(viewport_, sprite_batch_);
        // initialize ShapeRenderer
        shape_renderer_ = new ShapeRenderer();
        // initialize the Skin //Shade UI can be used under the CC BY license.
        //http://creativecommons.org/licenses/by/4.0/
        //https://ray3k.wordpress.com/artwork/shade-ui-skin-for-libgdx/
        skin_ = new Skin(Gdx.files.internal("ShadeUI/shadeui/uiskin.json"));
        // create switch to MainMenu button
        Button menu_screen_button = new TextButton("LEAVE GAME", skin_, "round"); // "small");
        menu_screen_button.setPosition(25.f, 25.f);

        // add InputListener to Button, and close app if Button is clicked
        menu_screen_button.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                change_screen_to_menu();
            }
        });

        // add exit button to Stage
        stage_.addActor(menu_screen_button);

        // load background music
        // good Beat for an intensive game --> you feel the Beat means you feel the Game
        background_music_ = Gdx.audio.newMusic(Gdx.files.internal("piano/Original_Beat.mp3"));
        background_music_.setLooping(true);
        background_music_.play();

        // create a Label with the Playersname string
        Label player_label = new Label("Current Player:          ", skin_, "title");
        player_label.setFontScale(1, 1);

        player_label.setPosition(
                (float) 10,
                (float) 570
        );
        stage_.addActor(player_label);

        player_label2 = new Label("                           ", skin_, "title");
        player_label2.setScale(20f, 20f);
        player_label2.setFontScale(1, 1);
        player_label2.setPosition(
                (float) 10,
                (float) 500
        );
        stage_.addActor(player_label2);

        // create a Label with the Playersname string
        Label swap2_rule = new Label("          \n " +
                                    "   SWAP2 Openingrule                             \n" +
                                    "                                                 \n" +
                                    "   1) First Player set two of your own stones &  \n" +
                                    "      one of the opponent.                       \n" +
                                    "   2) Second Player choose between               \n" +
                                    "      these three Option:                        \n" +
                                    "      1. You change the colour with              \n" +
                                    "         your opponent                           \n" +
                                    "      2. You set your colour &                   \n" +
                                    "          play with them                         \n" +
                                    "      3. You set one stone in your colour,       \n" +
                                    "         one in the opponents colour             \n" +
                                    "         and let your opponent choose if         \n" +
                                    "         he wants to change colours.             \n" +
                                    "                          ",
                skin_, "subtitle");
        swap2_rule.setFontScale(1, 1);

        swap2_rule.setPosition(
                (float) 930,
                (float) 410
        );
        stage_.addActor(swap2_rule);

        // load background texture
        background_texture_ = new Texture("texture/wood.jpg");
    }

    /**
     * Sendet die gesamte Gewinnhistorie.
     * Es gibt eine kurze Verzögerung von 1000ms für den Verbindungsaufabu für den Server.
     */
    private void send_winner_history(){
        try {
            SimpleClient client = new SimpleClient(new URI(String.format("ws://%s:%d", MainMenuScreen.host, MainMenuScreen.port)));
            client.connect();
            Thread.sleep(1000);
            client.send_history_push(gameScreenModel.getCurrent_player().getName(),
                    not_current_player().getName(),true,false);
            client.close();
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * @return Es wird der Spieler, der NICHT gerade am Zug ist zurückgegeben.
     */
    private Player not_current_player(){
        if (gameScreenModel.getCurrent_player()==Player.ONE){
            return Player.TWO;
        }else {
            return Player.ONE;
        }
    }

    /**
     * Hier werden die Namen nach dem Spiel wieder auf deren Defaultwerte zurückgesetzt und man kommt zurück auf den Menu Screen.
     */
    private void change_screen_to_menu() {
        Player.ONE.setName("Player 1");
        Player.TWO.setName("Player 2");
        parent_.change_screen(ScreenEnum.MENU);
    }


    /**
     * Interpolate between RGB(A) values.
     * Inspired by: https://stackoverflow.com/a/21010385/5380008
     *
     * @param color1   first color to mix
     * @param color2   second color to mix
     * @param fraction percentage 0.f-1.f
     * @return {@link Color} mixed color
     * @author Dennis Jehle
     */
    private Color mix(Color color1, Color color2, float fraction) {
        // calculated the mixed RGB values
        float r = (color2.r - color1.r) * fraction + color1.r;
        float g = (color2.g - color1.g) * fraction + color1.g;
        float b = (color2.b - color1.b) * fraction + color1.b;
        // return the mixed color
        return new Color(r, g, b, 1.f);
    }

    /**
     * Called when this screen becomes the current screen for a {@link Game}.
     * @author Dennis Jehle & Ibtsam Ali Mahmood
     *
     */
    @Override
    public void show() {
        multiplexer = new InputMultiplexer();
        Gdx.input.setInputProcessor(multiplexer);

        //Mouse click & game piece set
        multiplexer.addProcessor(new InputAdapter() {
            /**
             * Die Steine werden gesetzt. Wenn die Position frei ist wird dort bei einem klick auf die linke Maustauste
             * ein Stein, falls die Position schon belegt ist, wird keiner gesetzt.
             * @param screenX Ist der X-Achsen Abschnitt
             * @param screenY Ist der Y-Achsen Abchnitt
             * Inspiriert von https://stackoverflow.com/questions/17644429/libgdx-mouse-just-clicked
             * @param pointer ist der Mauszeiger
             * @param button  ist die Taste die gedrückt wurde
             * @return true wird zurückgegeben, wenn die Position frei ist, false falls nicht.
             * Hier werden die Regeln überprüft. Die Siegbedingung sowie die Swap2 Regel.
             * Es werden die dazugehörigen Dialogfelder aufgerufen.
             * @see GameScreenModel dort sind die ganzen Funktionen der Spielregeln.
             */
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (button != Input.Buttons.LEFT || pointer > 0) return false;
                Tuple tiles_position = gameScreenModel.findTilesPosition(screenX, Gdx.graphics.getHeight() - screenY);
                if (tiles_position == null) return false;
                boolean pos_free = gameScreenModel.setGamestone_position((int) tiles_position.first, (int) tiles_position.second);
                if (!pos_free) {
                    return false;
                }

                if (gameScreenModel.win_condition()){
                    show_winner_dialog();
                    send_winner_history();
                }

                int c = gameScreenModel.handle_rules_after_gamestone();

                switch (c){
                    case 1:
                        gameScreenModel.change_player();
                        show_openingrule_dialog();
                        break;
                    case 2:
                        show_change_player_colour_dialog();
                        break;
                }

                return true;
            }

        });

        // InputProcessor for Stage
        multiplexer.addProcessor(stage_);
        //show Dialog
        show_set_name_dialog();
    }

    /**
     * Inspiriert von: https://alvinalexander.com/source-code/how-create-libgdx-dialog-skin-example/
     * Hier sind die Funktionen der verschiedenen Dialogfelder.
     * Es gibt folgende Dialogfelder: Beim eingeben der Spielernamen, bei der Auswahl der Option für die SWAP2 Regel,
     * für die Option die Farben zu tauschen und beim eintreffen der Sigebedingung.
     */
    private void show_change_player_colour_dialog() {
        //show Colour change dialog
        Dialog dialog = new Dialog("    Change colour?      ", skin_) {
            protected void result(Object obj) {
                boolean ya_no = (boolean) obj;
                Gdx.input.setInputProcessor(multiplexer);
                if (ya_no) {
                    gameScreenModel.change_player_colour();
                    gameScreenModel.change_player();
                }
            }

        };
        dialog.text(gameScreenModel.getCurrent_player().getName() + " Do you want to change your colour?");
        dialog.button("Yes", true);
        dialog.button("No", false);
        dialog.show(stage_);

        Gdx.input.setInputProcessor(stage_);

    }

    private void show_openingrule_dialog() {
        //show Openingdialog
        Dialog dialog = new Dialog("Openingrule", skin_) {
            protected void result(Object obj) {
                gameScreenModel.setOpening_rule((int) obj);
                Gdx.input.setInputProcessor(multiplexer);
                if (gameScreenModel.getOpening_rule() == 1) {
                    gameScreenModel.change_player_colour();
                }
                //Openingrule 2 not neccesary beceause of else


            }

        };
        dialog.text(gameScreenModel.getCurrent_player().getName() + " choose your option to continue: ");
        dialog.button("Option 1", 1);
        dialog.button("Option 2", 2);
        dialog.button("Option 3", 3);
        dialog.show(stage_);

        Gdx.input.setInputProcessor(stage_);

    }


    private void show_set_name_dialog() {

        //show Namedialog
        Dialog dialog_name = new Dialog("Set your Playername", skin_) {
            protected void result(Object obj) {
                if ((boolean) obj) {
                    Player.ONE.setName(dialog_player_input1.getText());
                    Player.TWO.setName(dialog_player_input2.getText());

                }
                System.out.println(Player.ONE.getName() + "--------" + Player.TWO.getName());
                System.out.println("result " + obj);
                Gdx.input.setInputProcessor(multiplexer);
            }
        };

        dialog_player_input1 = new TextField(Player.ONE.getName(), skin_);
        dialog_name.text("Players Name: ");
        dialog_name.button("OK", true);
        dialog_name.button("Cancel", false);
        dialog_name.key(Input.Keys.ENTER, true);
        dialog_name.key(Input.Keys.ESCAPE, false);
        dialog_name.getContentTable().row();
        dialog_name.getContentTable().add(dialog_player_input1).width(135);

        dialog_player_input2 = new TextField(Player.TWO.getName(), skin_);
        dialog_name.getContentTable().row();
        dialog_name.getContentTable().add(dialog_player_input2).width(135);
        dialog_name.show(stage_);
        stage_.setKeyboardFocus(dialog_player_input2);
        stage_.unfocusAll();
        Gdx.input.setInputProcessor(stage_);


    }

    private void show_winner_dialog() {
        //show winner dialog
        Dialog dialog = new Dialog("        Winner!         ", skin_) {
            protected void result(Object obj) {
                Gdx.input.setInputProcessor(multiplexer);
                if ((boolean)obj) {
                    change_screen_to_menu();
                }else {

                }
            }
        };
        dialog.text(gameScreenModel.getCurrent_player().getName() + " has won!");
        dialog.button("Back to Menu", true);
        dialog.show(stage_);

        Gdx.input.setInputProcessor(stage_);
    }

    /**
     * Called when the screen should render itself.
     *
     * @param delta The time in seconds since the last render.
     * @author Dennis Jehle & Ibtsam Ali Mahmood
     *
     * Visualisierung des Spielfeldes und der Spielsteine.
     */
    @Override
    public void render(float delta) {
        // clear the client area (Screen) with the clear color (black)
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // draw background graphic
        // note: it is not necessary to use two SpriteBatch blocks
        // the background rendering is separated from the ParticleEffect rendering
        // for the sake of clarity
        sprite_batch_.begin();
        sprite_batch_.draw(background_texture_, 0, 0, viewport_.getScreenWidth(), viewport_.getScreenHeight());
        sprite_batch_.end();

        // update camera
        camera_.update();

        // update the current SpriteBatch
        sprite_batch_.setProjectionMatrix(camera_.combined);

        // gather necessary information for grid drawing
        float screen_width = Gdx.graphics.getWidth();
        float screen_height = Gdx.graphics.getHeight();
        float column_height = screen_height - 2.f * padding;
        float row_width = column_height;
        float offset = row_width / ((float) grid_size_ - 1.f);
        float top_left_x = screen_width / 2.f - row_width / 2.f;

        // draw grid
        shape_renderer_.begin(ShapeType.Filled);
        for (int i = 0; i < grid_size_; i++) {
            float fraction = (float) (i + 1) / (float) grid_size_;
            shape_renderer_.rectLine(
                    top_left_x + i * offset, padding + column_height
                    , top_left_x + i * offset, padding
                    , line_width
                    , mix(red_, red_, fraction)
                    , mix(red_, white_, fraction)
            );
            shape_renderer_.rectLine(
                    top_left_x, padding + column_height - i * offset
                    , top_left_x + row_width, padding + column_height - i * offset
                    , line_width
                    , mix(red_, red_, fraction)
                    , mix(red_, white_, fraction)
            );
        }
        shape_renderer_.end();

        //getting the calculated Gamestone positions
        gamestone_positions = gameScreenModel.getGamestone_positions();
        //render the gamestone
        visualize_gamestones(gamestone_positions);
        //Display the current Player
        player_label2.setText(gameScreenModel.getCurrent_player().getName());

        // update the Stage
        stage_.act(delta);
        // draw the Stage
        stage_.draw();


    }

    /**
     * Funktion um den Stein zu visualisieren
     * @param gamestone_positions Hier werden die berechneten Positionen für den Spielstein übergeben
     * @see GameScreenModel
     */
    private void visualize_gamestones(Player[][] gamestone_positions) {
        for (int x = 0; x < grid_size_; x++) {
            for (int y = 0; y < grid_size_; y++) {
                if (gamestone_positions[x][y] != null) {
                    Tuple pixel = gameScreenModel.findPixels(x, y);
                    Player p = gamestone_positions[x][y];
                    setGamestone(p.getColour(), (int) pixel.first, (int) pixel.second);

                }
            }
        }
    }


    /**
     * Die funktion um den Stein zu setzten
     * @param colour Die farbe der Steine
     * @param x_pixel die Pixel Koordinate auf der X Achse
     * @param y_pixel die Pixel Koordinate auf der Y Achse
     */
    private void setGamestone(Color colour, int x_pixel, int y_pixel) {
        shape_renderer_.setColor(colour);
        shape_renderer_.begin(ShapeType.Filled);
        shape_renderer_.circle(x_pixel, y_pixel, 14);
        shape_renderer_.end();
    }

    public GameScreenModel getGameScreenModel() {
        return gameScreenModel;
    }

    /**
     * This method is called if the window gets resized.
     *
     * @param width  new window width
     * @param height new window height
     * @author Dennis Jehle
     * @see ApplicationListener#resize(int, int)
     */
    @Override
    public void resize(int width, int height) {
        // could be ignored because you cannot resize the window at the moment
    }

    /**
     * This method is called if the application lost focus.
     *
     * @author Dennis Jehle
     * @see ApplicationListener#pause()
     */
    @Override
    public void pause() {

    }

    /**
     * This method is called if the applaction regained focus.
     *
     * @author Dennis Jehle
     * @see ApplicationListener#resume()
     */
    @Override
    public void resume() {

    }

    /**
     * Called when this screen is no longer the current screen for a {@link Game}.
     *
     * @author Dennis Jehle
     */
    @Override
    public void hide() {
        background_music_.stop();
    }

    /**
     * Called when this screen should release all resources.
     *
     * @author Dennis Jehle
     */
    @Override
    public void dispose() {
        background_music_.dispose();
        background_texture_.dispose();
        skin_.dispose();
        stage_.dispose();
        sprite_batch_.dispose();
    }
}
