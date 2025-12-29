package com.pippobet.view.fx;

import com.pippobet.controller.BetController;
import com.pippobet.model.Bet;
import com.pippobet.view.BetView;
import java.util.List;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Builder;
import javafx.util.converter.DoubleStringConverter;

public class BetFXViewBuilder implements Builder<Region>, BetView
{
    public static final String HOME_TEAM_INPUT_ID = "homeTeamInput";
    public static final String AWAY_TEAM_INPUT_ID = "awayTeamInput";
    public static final String OUTCOME_INPUT_ID = "outcomeInput";
    public static final String ODD_INPUT_ID = "oddInput";
    public static final String ADD_BUTTON_INPUT_ID = "addButton";
    public static final String DELETE_BUTTON_INPUT_ID = "deleteButton";

    private final TableView<Bet> table;
    private BetController betController;
    private TextField homeTeamField;
    private TextField awayTeamField;
    private TextField outcomeField;
    private TextField oddField;
    private Button addButton;
    private Button deleteButton;
    private final ObservableList<Bet> betList;


    public BetFXViewBuilder(TableView<Bet> table)
    {
        this.table = table;
        this.betList = FXCollections.observableArrayList();
        table.setItems(betList);
        initializeInputFields();
    }


    public void setEventController(BetController eventController)
    {
        this.betController = eventController;
    }


    @Override
    public void updateBets(List<Bet> bets)
    {
        table.setItems(FXCollections.observableList(bets));
    }


    @Override
    public Region build()
    {
        TableColumn<Bet, String> homeTeamCol = new TableColumn<>("Home team");
        homeTeamCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getHomeTeam()));
        homeTeamCol.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<Bet, String> awayTeamCol = new TableColumn<>("Away team");
        awayTeamCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAwayTeam()));
        awayTeamCol.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<Bet, String> outcomeCol = new TableColumn<>("Outcome");
        outcomeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOutcome()));
        outcomeCol.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<Bet, Double> oddCol = new TableColumn<>("Odd");
        oddCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getOdd()).asObject());
        oddCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));

        table.getColumns().addAll(homeTeamCol, awayTeamCol, outcomeCol, oddCol);

        betList.clear();

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(10));

        Region addBetForm = createBetForm();
        layout.getChildren().addAll(addBetForm, table);

        betController.showAllBets();

        return layout;
    }


    private void initializeInputFields()
    {
        homeTeamField = new TextField();
        homeTeamField.setPromptText("Enter home team");
        homeTeamField.setId(HOME_TEAM_INPUT_ID);

        awayTeamField = new TextField();
        awayTeamField.setPromptText("Enter away team");
        awayTeamField.setId(AWAY_TEAM_INPUT_ID);

        outcomeField = new TextField();
        outcomeField.setPromptText("Enter outcome (1, X, 2)");
        outcomeField.setId(OUTCOME_INPUT_ID);

        oddField = new TextField();
        oddField.setPromptText("Enter odd");
        oddField.setId(ODD_INPUT_ID);

        addButton = new Button("Add Bet");
        addButton.setId(ADD_BUTTON_INPUT_ID);
        addButton.setOnAction(e -> addNewBet());

        deleteButton = new Button("Delete Bet");
        deleteButton.setId(DELETE_BUTTON_INPUT_ID);
        deleteButton.setOnAction(e -> deleteNewBet());
    }


    private void addNewBet()
    {
        String homeTeam = homeTeamField.getText().trim();
        String awayTeam = awayTeamField.getText().trim();
        String outcome = outcomeField.getText().trim();
        double odd = Double.parseDouble(oddField.getText().trim());

        Bet newBet = new Bet(homeTeam, awayTeam, outcome, odd);

        betController.addBet(newBet);

        clearForm();
    }


    private void deleteNewBet()
    {
        var betToDelete = table.getSelectionModel().getSelectedItem();
        betController.deleteBet(betToDelete);
    }


    private void clearForm()
    {
        homeTeamField.clear();
        awayTeamField.clear();
        outcomeField.clear();
        oddField.clear();
    }


    private Region createBetForm()
    {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));
        form.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-color: #f9f9f9;");

        form.add(new Label("Home Team:"), 0, 0);
        form.add(homeTeamField, 1, 0);

        form.add(new Label("Away Team:"), 2, 0);
        form.add(awayTeamField, 3, 0);

        form.add(new Label("Outcome:"), 0, 1);
        form.add(outcomeField, 1, 1);

        form.add(new Label("Odd:"), 2, 1);
        form.add(oddField, 3, 1);

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.getChildren().add(addButton);
        buttonBox.getChildren().add(deleteButton);
        form.add(buttonBox, 1, 2, 2, 1);

        return form;
    }
}
