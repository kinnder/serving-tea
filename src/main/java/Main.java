import jade.Boot;

public class Main {
	public static void main(String[] args) {
				//String[] parameters = new String[2];
				//parameters[0] = "-gui";
				//parameters[1] = "waitress:waitress.Waitress;";
				
				String[] parameters = new String[] { "-gui",
						 "-host", "localhost",
						//"waitress:waitress.Waitress;customer:TheGreatestCustomerEverrrr.CoolCustomer_Agent;" 
						 "CoffeeMachine:coffeeMachine.CoffeeMachine;Boiler:Robots.Robot_Agent;"};
				Boot.main(parameters);
				
		
			}
}
