package com.minimalide.ide;

import com.minimalide.gals.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class CompilerController {

    private TextArea inputArea;
    private TextArea outputArea;
    private boolean outputVisible = true;
    private Stage tabelaStage;

    public VBox buildLayout(Stage stage) {
        // Barra Botoes
        Region toolBarSpacer = new Region();
        HBox.setHgrow(toolBarSpacer, Priority.ALWAYS);

        Button tabelaBtn = new Button("⊞  Tabela");
        tabelaBtn.getStyleClass().add("compile-btn");
        tabelaBtn.setOnAction(e -> {
            if (tabelaStage != null) {
                tabelaStage.show();
                tabelaStage.toFront();
            }
        });

        Button compileBtn = new Button("▶  Compilar");
        compileBtn.getStyleClass().add("compile-btn");
        compileBtn.setOnAction(e -> compile());

        HBox toolbar = new HBox(toolBarSpacer, tabelaBtn, compileBtn);
        toolbar.getStyleClass().add("toolbar");

        // Campo Input
        inputArea = new TextArea();
        inputArea.getStyleClass().add("code-area");
        VBox.setVgrow(inputArea, Priority.ALWAYS);

        inputArea.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.TAB) {
                int caretPos = inputArea.getCaretPosition();
                inputArea.insertText(caretPos, "  ");
                e.consume();
            }
        });

        TextArea lineNumbers = new TextArea("1");
        lineNumbers.setEditable(false);
        lineNumbers.setFocusTraversable(false);
        lineNumbers.setPrefWidth(52);
        lineNumbers.setMinWidth(52);
        lineNumbers.setMaxWidth(52);
        lineNumbers.getStyleClass().add("line-numbers");

        inputArea.textProperty().addListener((obs, oldVal, newVal) -> {
            int lines = newVal.split("\n", -1).length;
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= lines; i++) {
                sb.append(i);
                if (i < lines) sb.append("\n");
            }
            lineNumbers.setText(sb.toString());
        });

        inputArea.scrollTopProperty().addListener((obs, o, n) ->
            lineNumbers.setScrollTop(n.doubleValue())
        );

        HBox editorPane = new HBox(lineNumbers, inputArea);
        editorPane.getStyleClass().add("editor-pane");
        HBox.setHgrow(inputArea, Priority.ALWAYS);
        VBox.setVgrow(editorPane, Priority.ALWAYS);

        // Campo Output para exibir os erros
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefRowCount(8);
        outputArea.getStyleClass().add("output-area");

        Label outputLabel = new Label("OUTPUT");
        outputLabel.getStyleClass().add("output-label");

        Button toggleBtn = new Button("▼ ocultar");
        toggleBtn.getStyleClass().add("output-toggle");
        toggleBtn.setOnAction(e -> {
            outputVisible = !outputVisible;
            outputArea.setVisible(outputVisible);
            outputArea.setManaged(outputVisible);
            toggleBtn.setText(outputVisible ? "▼ ocultar" : "▶ mostrar");
        });

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        HBox outputHeader = new HBox(8, outputLabel, headerSpacer, toggleBtn);
        outputHeader.getStyleClass().add("output-header");

        VBox outputPane = new VBox(outputHeader, outputArea);
        outputPane.getStyleClass().add("output-pane");

        VBox root = new VBox(toolbar, editorPane, outputPane);
        root.getStyleClass().add("root-pane");
        return root;
    }

    private void abrirTabelaSimbolos(java.util.List<Simbolo> simbolos) {
        if (tabelaStage == null) {
            tabelaStage = new Stage();
            tabelaStage.setTitle("Tabela de Símbolos");
        }

        TableView<Simbolo> tabelaView = new TableView<>();
        tabelaView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabelaView.getStyleClass().add("symbol-table");

        TableColumn<Simbolo, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().nome));

        TableColumn<Simbolo, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().tipo));

        TableColumn<Simbolo, String> colCategoria = new TableColumn<>("Modalidade");
        colCategoria.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().categoria.name()));

        TableColumn<Simbolo, String> colEscopo = new TableColumn<>("Escopo");
        colEscopo.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().nivelEscopo)));

        TableColumn<Simbolo, String> colInicializado = new TableColumn<>("Inicializado");
        colInicializado.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().inicializado ? "sim" : "nao"));

        TableColumn<Simbolo, String> colUsado = new TableColumn<>("Usado");
        colUsado.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().usado ? "sim" : "nao"));

        tabelaView.getColumns().addAll(colNome, colTipo, colCategoria, colEscopo, colInicializado, colUsado);

        ObservableList<Simbolo> itens = FXCollections.observableArrayList(simbolos);
        tabelaView.setItems(itens);

        VBox layout = new VBox(tabelaView);
        VBox.setVgrow(tabelaView, Priority.ALWAYS);

        Scene scene = new Scene(layout, 700, 400);
        scene.getStylesheets().add(
            getClass().getResource("/com/minimalide/style.css").toExternalForm()
        );

        tabelaStage.setScene(scene);
        tabelaStage.show();
        tabelaStage.toFront();
    }

    private void compile() {
        String source = inputArea.getText();
        outputArea.clear();

        if (!outputVisible) {
            outputArea.setVisible(true);
            outputArea.setManaged(true);
            outputVisible = true;
        }

        if (source.isBlank()) {
            outputArea.setText("Nenhum código para compilar.");
            return;
        }
        try {
            Lexico lexico = new Lexico(source);
            Sintatico sintatico = new Sintatico();
            Semantico semantico = new Semantico();

            sintatico.parse(lexico, semantico);
            semantico.verificarNaoUsados();

            abrirTabelaSimbolos(semantico.getTabelaSimbolos());

            if (semantico.getWarnings().isEmpty()) {
                outputArea.setStyle("-fx-text-fill: #9cdcfe;");
                outputArea.setText("✔  Compilação concluída sem erros.");
            } else {
                StringBuilder msg = new StringBuilder("⚠  Compilação concluída com avisos.\n\n");
                for (String w : semantico.getWarnings()) {
                    msg.append("⚠ ").append(w).append("\n");
                }
                outputArea.setStyle("-fx-text-fill: #f1c40f;");
                outputArea.setText(msg.toString());
            }
        } catch (SyntacticError e) {
            outputArea.setStyle("-fx-text-fill: red;");
            outputArea.setText("✘  Erro Sintatico:\n\n" + e.getMessage());
        } catch (SemanticError e) {
            outputArea.setStyle("-fx-text-fill: red;");
            outputArea.setText("✘  Erro Semantico:\n\n" + e.getMessage());
        } catch (LexicalError e) {
            outputArea.setStyle("-fx-text-fill: red;");
            outputArea.setText("✘  Erro Lexico:\n\n" + e.getMessage());
        }
    }
}
