package com.mammb.code.editor;

import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.AccessibleRole;
import javafx.scene.Cursor;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class TextPane extends Region {

    private static final double lineHeight = Utils.getTextHeight(Fonts.main);

    private final Clipboard clipboard = Clipboard.getSystemClipboard();
    private final ScreenBuffer screenBuffer = new ScreenBuffer();
    private final Stage stage;
    private final TextLine textFlow;
    private final Caret caret;
    private final Selection selection;
    private final ImePalette imePalette;
    private final ScrollBar vScroll;
    private final HScrollBar hScroll;

    public TextPane(Stage stage) {

        this.stage = stage;
        setupStageTitle();

        setBackground(new Background(new BackgroundFill(Colors.bgColor, null, null)));
        setFocusTraversable(true);
        setAccessibleRole(AccessibleRole.TEXT_AREA);
        setCursor(Cursor.DEFAULT);

        setOnKeyPressed(this::handleKeyPressed);
        setOnScroll(this::handleScroll);
        setOnMouseClicked(this::handleMouseClicked);
        setOnMouseDragged(this::handleMouseDragged);
        setOnKeyTyped(this::handleInput);

        textFlow = new TextLine();
        screenBuffer.addDirtyListener(textFlow::handleDirty);

        caret = new Caret();
        caret.setLayoutX(textFlow.getPadding().getLeft());
        caret.setLayoutY(textFlow.getPadding().getTop());
        caret.setShape(textFlow.caretShape(0, true));

        screenBuffer.caretOffsetProperty().addListener(this::handleCaretMoved);
        screenBuffer.addListChangeListener(this::handleScreenTextChanged);
        screenBuffer.originIndexProperty().addListener(this::handleOriginMoved);

        selection = new Selection();

        imePalette = new ImePalette(textFlow, screenBuffer);
        setInputMethodRequests(imePalette.createInputMethodRequests());
        setOnInputMethodTextChanged(imePalette::handleInputMethod);

        BorderPane pane = new BorderPane();
        pane.prefHeightProperty().bind(heightProperty());
        pane.prefWidthProperty().bind(widthProperty());

        StackPane textStack = new StackPane(textFlow, caret, selection, imePalette);
        textStack.setFocusTraversable(true);
        textStack.setCursor(Cursor.TEXT);
        Pane main = new Pane(textStack);
        Pane left = new SidePanel(screenBuffer);
        left.setPadding(new Insets(4));

        pane.setCenter(main);
        pane.setLeft(left);
        getChildren().add(pane);

        layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue.getHeight() != newValue.getHeight()) {
                initScreenRowSize(newValue.getHeight());
            }
        });
        initScreenRowSize(getLayoutBounds().getHeight());
        initDropTarget();

        hScroll = new HScrollBar(screenBuffer);
        hScroll.layoutYProperty().bind(heightProperty().subtract(hScroll.getHeight()));
        hScroll.prefWidthProperty().bind(widthProperty());
        hScroll.thumbLengthProperty().bind(widthProperty().subtract(left.widthProperty()));
        hScroll.maxProperty().bind(textFlow.widthProperty());
        hScroll.visibleProperty().bind(widthProperty().subtract(left.widthProperty()).lessThan(textFlow.widthProperty()));
        textStack.layoutXProperty().bind(hScroll.valueProperty().multiply(-1));
        getChildren().add(hScroll);

        vScroll = new ScrollBar(screenBuffer);
        vScroll.layoutXProperty().bind(widthProperty().subtract(vScroll.getWidth() + 2));
        vScroll.prefHeightProperty().bind(main.heightProperty().subtract(hScroll.getHeight()));
        vScroll.thumbLengthProperty().bind(screenBuffer.screenRowSizeProperty().subtract(hScroll.getHeight()));
        vScroll.valueProperty().bind(screenBuffer.originRowIndexProperty());
        vScroll.maxProperty().bind(screenBuffer.scrollMaxLinesProperty());
        getChildren().add(vScroll);

    }

    private void setupStageTitle() {
        Path path = screenBuffer.getPath();
        String title = (path == null) ? "untitled" : path.toString();
        stage.setTitle(title);
    }

    private void initScreenRowSize(double height) {

        screenBuffer.setupScreenRowSize(Math.max((int) Math.ceil(height / lineHeight), 1));
    }

    private void initDropTarget() {
        setOnDragOver(event -> {
            Dragboard board = event.getDragboard();
            if (board.hasFiles()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
        });
        setOnDragDropped(event -> {
            Dragboard board = event.getDragboard();
            if (board.hasFiles()) {
                Optional<Path> maybePath = board.getFiles().stream().findFirst().map(File::toPath);
                if (maybePath.isPresent()) {
                    Path path = maybePath.get();
                    textFlow.init(getExtension(path));
                    screenBuffer.open(path);
                    setupStageTitle();
                }
                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
        });
    }


    private void handleScreenTextChanged(ListChangeListener.Change<? extends String> change) {
        while (change.next()) {
            if (change.wasRemoved()) {
                textFlow.remove(
                    screenBuffer.getOriginRowIndex() + change.getFrom(),
                    change.getFrom(),
                    change.getFrom() + change.getRemovedSize());
            }
            if (change.wasAdded()) {
                textFlow.addAll(
                    screenBuffer.getOriginRowIndex() + change.getFrom(),
                    change.getFrom(),
                    change.getAddedSubList());
            }
        }
    }


    void handleCaretMoved(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        if (newValue.intValue() < 0 || newValue.intValue() > screenBuffer.charCountOnScreen()) {
            caret.disable();
        } else {
            caret.setShape(textFlow.caretShape(newValue.intValue(), true));
        }
        if (selection.on()) {
            selection.handleCaretMoved(newValue.intValue(), textFlow::rangeShape);
        }
    }

    void handleOriginMoved(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        if (selection.on()) {
            selection.handleOriginMoved(oldValue.intValue(), newValue.intValue());
        }
    }

    private void handleInput(KeyEvent e) {

        if (e.getCode().isFunctionKey() || e.getCode().isNavigationKey() ||
            e.getCode().isArrowKey() || e.getCode().isModifierKey() ||
            e.getCode().isMediaKey() || !Keys.controlKeysFilter.test(e) ||
            e.getCharacter().length() == 0) {
            return;
        }
        int ascii = e.getCharacter().getBytes()[0];
        if (ascii < 32 || ascii == 127) {
            // 127:DEL
            if (ascii != 9 && ascii != 10 && ascii != 13) {
                // 9:HT 10:LF 13:CR
                return;
            }
        }
        if (selection.on()) {
            selectionDelete();
        }
        // Enter key : 13:CR -> replace to 10:LF
        screenBuffer.add(e.getCharacter().replace('\r', '\n'));
    }


    private void handleKeyPressed(KeyEvent e) {

        if (imePalette.getImeOn()) {
            if (e.getCode() == KeyCode.ESCAPE) {
                imePalette.setImeOn(false);
            }
            return;
        }

        if (Keys.SC_O.match(e)) {
            File file = Utils.fileChooseOpen(stage, screenBuffer.getPath());
            if (file != null) screenBuffer.open(file.toPath());
            setupStageTitle();
            return;
        }
        if (Keys.SC_S.match(e)) {
            if (screenBuffer.getPath() != null) {
                screenBuffer.save();
            } else {
                File file = Utils.fileChooseSave(stage, screenBuffer.getPath());
                if (file != null) screenBuffer.saveAs(file.toPath());
                setupStageTitle();
            }
            return;
        }
        if (Keys.SC_SA.match(e)) {
            File file = Utils.fileChooseSave(stage, screenBuffer.getPath());
            if (file != null) screenBuffer.saveAs(file.toPath());
            setupStageTitle();
            return;
        }
        if (Keys.SC_C.match(e)) {
            copyToClipboard();
            return;
        }
        if (Keys.SC_V.match(e)) {
            pasteFromClipboard();
            return;
        }
        if (Keys.SC_X.match(e)) {
            cutToClipboard();
            return;
        }
        if (Keys.SC_Z.match(e)) {
            screenBuffer.undo();
            return;
        }
        if (Keys.SC_Y.match(e) || Keys.SC_SZ.match(e)) {
            screenBuffer.redo();
            return;
        }

        switch (e.getCode()) {
            case LEFT       -> selectOr(e, ke -> screenBuffer.prev());
            case RIGHT      -> selectOr(e, ke -> screenBuffer.next());
            case UP         -> selectOr(e, ke -> screenBuffer.prevLine());
            case DOWN       -> selectOr(e, ke -> screenBuffer.nextLine());
            case HOME       -> selectOr(e, ke -> screenBuffer.home());
            case END        -> selectOr(e, ke -> screenBuffer.end());
            case PAGE_UP    -> selectOr(e, ke -> screenBuffer.pageUp());
            case PAGE_DOWN  -> selectOr(e, ke -> screenBuffer.pageDown());
            case DELETE     -> delete();
            case BACK_SPACE -> backSpace();
            case F1         -> screenBuffer.inspect();
            case ESCAPE     -> screenBuffer.reset();
            default -> { }
        }
    }

    private void delete() {
        if (selection.on()) {
            selectionDelete();
        } else {
            screenBuffer.delete(1);
        }
    }

    private void backSpace() {
        if (selection.on()) {
            selectionDelete();
        } else {
            screenBuffer.backSpace();
        }
    }

    private void handleScroll(ScrollEvent e) {
        if (e.getEventType() == ScrollEvent.SCROLL) {
            if (e.getDeltaY() > 2)  screenBuffer.scrollPrev(2);
            else if (e.getDeltaY() > 0)  screenBuffer.scrollPrev(1);
            else if (e.getDeltaY() < -2) screenBuffer.scrollNext(2);
            else if (e.getDeltaY() < 0)  screenBuffer.scrollNext(1);
        }
    }

    private void handleMouseClicked(MouseEvent e) {

        if (imePalette.getImeOn()) {
            return;
        }

        if (selection.on()) {
            if (selection.isDragging()) {
                selection.releaseDragging();
            } else {
                selection.clear();
            }
        }

        HitInfo hit = textFlow.hitTest(textFlow.sceneToLocal(new Point2D(e.getSceneX(), e.getSceneY())));
        if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 1) {
            screenBuffer.moveCaret(hit.getInsertionIndex());
        } else if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
            // word select
            int[] range = screenBuffer.wordRange(hit.getInsertionIndex());
            selection.start(range[0]);
            screenBuffer.moveCaret(range[1]);
        }
    }

    private void handleMouseDragged(MouseEvent e) {
        if (imePalette.getImeOn()) {
            return;
        }
        if (!e.getButton().equals(MouseButton.PRIMARY)) return;
        HitInfo hit = textFlow.hitTest(textFlow.sceneToLocal(new Point2D(e.getSceneX(), e.getSceneY())));
        screenBuffer.moveCaret(hit.getInsertionIndex());
        if (!selection.isDragging()) {
            selection.startDrag(hit.getInsertionIndex());
        }
    }

    private void pasteFromClipboard() {
        if (selection.on()) {
            selectionDelete();
        }
        if (clipboard.hasString()) {
            screenBuffer.add(clipboard.getString());
        }
    }

    public void copyToClipboard() {
        if (selection.off()) {
            return;
        }
        String text = "";
        if (selection.getOpen() < selection.getClose()) {
            text = screenBuffer.peekString(
                screenBuffer.getOriginIndex() + selection.getOpen(),
                screenBuffer.getOriginIndex() + selection.getClose());
        } else if (selection.getOpen() > selection.getClose()) {
            text = screenBuffer.peekString(
                screenBuffer.getOriginIndex() + selection.getClose(),
                screenBuffer.getOriginIndex() + selection.getOpen());
        }
        if (text != null && !text.isBlank()) {
            Map<DataFormat, Object> content = new HashMap<>();
            content.put(DataFormat.PLAIN_TEXT, text);
            clipboard.setContent(content);
        }
    }

    public void cutToClipboard() {
        if (selection.off()) {
            return;
        }
        copyToClipboard();
        selectionDelete();
    }

    public void selectionDelete() {
        if (selection.off()) {
            return;
        }
        if (selection.getOpen() < selection.getClose()) {
            int len = selection.getClose() - selection.getOpen();
            int open = selection.getOpen();
            selection.clear();
            screenBuffer.moveCaret(open);
            screenBuffer.delete(len);

        } else if (selection.getOpen() > selection.getClose()) {
            int len = selection.getOpen() - selection.getClose();
            selection.clear();
            screenBuffer.delete(len);
        }
    }


    private void selectOr(KeyEvent e, Consumer<KeyEvent> consumer) {
        if (e.isShiftDown() && selection.off()) {
            selection.start(screenBuffer.getCaretOffset());
        } else if (!e.isShiftDown() && selection.on()) {
            selection.clear();
        }
        consumer.accept(e);
    }

    private static String getExtension(Path path) {
        if (path == null) {
            return "";
        }
        String fileName = path.getFileName().toString();
        return fileName.substring(fileName.lastIndexOf('.'));
    }

}
