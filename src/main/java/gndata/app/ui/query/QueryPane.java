package gndata.app.ui.query;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.apache.jena.atlas.lib.StrUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * An extension of the grid pane that can add/remove text fields.
 */
public class QueryPane extends GridPane {

    private Button addMore;
    public Button submit;

    public QueryPane() {
        super();

        addMore = new Button("+");
        addMore.setOnAction(e -> addEmptyQueryRow());
        GridPane.setConstraints(addMore, 3, 0);

        submit = new Button("Submit");
        GridPane.setConstraints(submit, 0, 0);

        getChildren().addAll(submit, addMore);

        addQueryRow("?x", "?y", "?z");
    }

    private void addEmptyQueryRow() {
        addQueryRow("", "", "");
    }

    private void addQueryRow(String subj, String pred, String obj) {
        int rowIndex = GridPane.getRowIndex(addMore);

        GridPane.setRowIndex(addMore, rowIndex + 1);
        GridPane.setRowIndex(submit, rowIndex + 1);

        TextField to = new TextField(subj);
        TextField tp = new TextField(pred);
        TextField ts = new TextField(obj);
        Button remRow = new Button("-");

        GridPane.setConstraints(to, 0, rowIndex);
        GridPane.setConstraints(tp, 1, rowIndex);
        GridPane.setConstraints(ts, 2, rowIndex);
        GridPane.setConstraints(remRow, 3, rowIndex);

        getChildren().addAll(to, tp, ts, remRow);

        remRow.setOnAction(e -> {
            int index = (int) remRow.getProperties().get("gridpane-row");

            List<Node> toRemove = new ArrayList<Node>();
            getChildren().forEach(node -> {
                if (GridPane.getRowIndex(node) == index &&
                        getChildren().size() > 5) {
                    toRemove.add(node);
                }
            });

            getChildren().removeAll(toRemove);
        });
    }

    private List<String> getTextInputs() {
        List<String> lst = new ArrayList<>();

        getChildren().forEach(node -> {
            if (node instanceof TextField) {
                TextField field = (TextField) node;
                lst.add(field.getText());
            }
        });

        return lst;
    }

    public String readQuery() {
        List<String> lst = getTextInputs();

        Set<String> selectors = new LinkedHashSet<>();
        lst.forEach(input -> {
            if (input.length() > 0 && input.substring(0, 1).equals("?")) {
                selectors.add(input);
            }
        });

        List<String> conditions = new ArrayList<>();
        for(int i = 0; i < lst.size()/3; i++) {
            conditions.add(StrUtils.strjoin(" ",
                lst.get(i), lst.get(i + 1), lst.get(i + 2)
            ));
        }

        return StrUtils.strjoinNL(
                "SELECT ",
                String.join(" ", selectors),
                "WHERE { ",
                String.join(" . ", conditions),
                "}"
        );
    }
}
