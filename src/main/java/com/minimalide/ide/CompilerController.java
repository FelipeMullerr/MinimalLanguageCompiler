package com.minimalide.ide;


import com.minimalide.gals.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class CompilerController {

    private TextArea inputArea;
    private TextArea outputArea;
    private boolean outputVisible = true;

    public VBox buildLayout(Stage stage) {

        // Barra Botoes
        Region toolBarSpacer = new Region();
        HBox.setHgrow(toolBarSpacer, Priority.ALWAYS);

        Button compileBtn = new Button("▶  Compilar");
        compileBtn.getStyleClass().add("compile-btn");
        compileBtn.setOnAction(e -> compile());

        HBox toolbar = new HBox(toolBarSpacer,compileBtn);
        toolbar.getStyleClass().add("toolbar");

        // Campo Input
        inputArea = new TextArea();
        inputArea.getStyleClass().add("code-area");
        VBox.setVgrow(inputArea, Priority.ALWAYS);

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
                lineNumbers.setScrollTop(n.doubleValue()));

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
        try{
            Lexico lexico = new Lexico(source);

            Sintatico sintatico = new Sintatico();
            Semantico semantico = new Semantico();

            sintatico.parse(lexico, semantico);
        } catch (SyntacticError e) {
            outputArea.setText("✘  Erro Sintatico:\n\n" + e.getMessage());
        } catch (SemanticError e) {
            outputArea.setText("✘  Erro Semantico:\n\n" + e.getMessage());
        } catch (LexicalError e) {
            outputArea.setText("✘  Erro Lexico:\n\n" + e.getMessage());
        }

        outputArea.setText("✔  Compilação concluída sem erros.\n\n" + "   Análise léxica    → OK\n" + "   Análise sintática → OK\n" + "   Análise semântica → OK\n");

    }
}