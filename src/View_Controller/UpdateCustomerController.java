/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View_Controller;

import Models.Address;
import Models.City;
import Models.Country;
import Models.Customer;
import static Models.Customer.getCustomerList;
import static Util.mainDB.dbConnect;
import static Util.mainDB.getConn;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Dane Schlea
 */
public class UpdateCustomerController implements Initializable {
    
    //UI items
    @FXML
    private Label labelManCust;
    @FXML
    private Label mCustName;
    @FXML
    private Label mCustAddress;
    @FXML
    private Label mCustCity;
    @FXML
    private Label mCustAddressCont;
    @FXML
    private Label mCustPostal;
    @FXML
    private Label mCustCountry;
    @FXML
    private Label mCustPhone;
    @FXML
    private TextField mCustNameField;
    @FXML
    private TextField mCustAddressField;
    @FXML
    private TextField mCustAddressContField;
    @FXML
    private TextField mCustCityField;
    @FXML
    private TextField mCustPostalField;
    @FXML
    private TextField mCustCountryField;
    @FXML
    private TextField mCustPhoneField;
    @FXML
    private Button mCustSave;
    @FXML
    private Button mCustCancel;
    
    //modifiers
    private Customer customer;
    int updateCustomerIndex = CustomersController.getUpdateCustomerIndex();
    private String oldName;
    private String oldAddress;
    private String oldPhone;
    
    @FXML
    private void handleUpdateCustomerCancel(ActionEvent event) throws IOException{
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initModality(Modality.NONE);
        alert.setTitle("Confirm");
        alert.setHeaderText("Confirm cancellation");
        alert.setContentText("Are you sure you want to cancel?");
        Optional<ButtonType> result = alert.showAndWait();
        
        if(result.get() == ButtonType.OK){
            Parent updateCustomerCancel = FXMLLoader.load(getClass().getResource("Customers.fxml"));
            Scene scene = new Scene(updateCustomerCancel);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
        }
        else{
            System.out.println("Operation cancelled.");
        }
    }
    
    //save customer to the database
    @FXML
    private void handleUpdateCustomerSave(ActionEvent event) throws IOException{
        String name = mCustNameField.getText();
        String addr = mCustAddressField.getText();
        String addr2 = mCustAddressContField.getText();
        String cityName = mCustCityField.getText();
        String postal = mCustPostalField.getText();
        String country = mCustCountryField.getText();
        String phone = mCustPhoneField.getText();       
        if(Customer.customerValidate(name) == false || Address.addressValidate(addr, postal, phone) == false || City.cityValidate(cityName) == false || Country.countryValidate(country) == false){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initModality(Modality.NONE);
            alert.setTitle("Error");
            alert.setHeaderText("Error submitting customer");
            alert.setContentText("One or more invalid fields.");
            alert.showAndWait();
        }
        else{
            try{
                //get cityId
                dbConnect();
                PreparedStatement prepCity = getConn().prepareStatement("SELECT cityId FROM city WHERE city = ?");
                prepCity.setString(1, cityName);
                ResultSet resCity = prepCity.executeQuery();
                while(resCity.next()){
                    int rCity = (int) resCity.getObject("cityId");
                    
                //update data for address first to get address key
                PreparedStatement prepS = getConn().prepareStatement("UPDATE address "
                                + "SET address = ?, address2 = ?, cityId = ?, postalCode = ?, phone = ?, lastUpdate = CURRENT_TIMESTAMP, lastUpdateBy = ? WHERE address = ? AND phone = ?");
                prepS.setString(1, addr);
                prepS.setString(2, addr2);
                prepS.setInt(3, rCity);
                prepS.setString(4, postal);
                prepS.setString(5, phone);
                prepS.setString(6, LoginController.currentUser);
                prepS.setString(7, oldAddress);
                prepS.setString(8, oldPhone);
                prepS.executeUpdate();
                
                //Get addressId
                PreparedStatement prepAdd = getConn().prepareStatement("SELECT addressId FROM address WHERE address = ?");
                prepAdd.setString(1, addr);
                ResultSet resAdd = prepAdd.executeQuery();
                while(resAdd.next()){
                    int rAdd = (int) resAdd.getObject("addressId");
                
                //update data for customer
                PreparedStatement pState = getConn().prepareStatement("UPDATE customer "
                        + "SET customerName = ?, addressId = ?, active = ?, lastUpdate = CURRENT_TIMESTAMP, lastUpdateBy = ? WHERE customerName = ?");
                pState.setString(1, name);
                pState.setInt(2, rAdd);
                pState.setInt(3, 1);
                pState.setString(4, LoginController.currentUser);
                pState.setString(5, oldName);
                pState.executeUpdate();
                
                //Go back to customers window
                Parent customersCancel = FXMLLoader.load(getClass().getResource("Customers.fxml"));
                Scene scene = new Scene(customersCancel);
                Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
                window.setScene(scene);
                window.show();
                    }
                }
            } catch (SQLException e) {
                System.out.println("SQL error");
            }
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //get list and field data
        customer = getCustomerList().get(updateCustomerIndex);
        String customerName = customer.getCustomerName();
        String address = customer.getAddress();
        String addressTwo = customer.getAddressTwo();
        String city = customer.getCity();
        String postal = customer.getPostal();
        String country = customer.getCountry();
        String phone = customer.getPhone();
        //update fields
        mCustNameField.setText(customerName);
        mCustAddressField.setText(address);
        mCustAddressContField.setText(addressTwo);
        mCustCityField.setText(city);
        mCustPostalField.setText(postal);
        mCustCountryField.setText(country);
        mCustPhoneField.setText(phone);
        //set old info for reference
        oldCustData(customerName, address, phone);
    }    
    
    //make variables from old info
    public void oldCustData(String name, String address, String phone){
       oldName = name;
       oldAddress = address;
       oldPhone = phone;
    }
}
