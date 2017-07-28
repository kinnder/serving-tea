package HomeWork;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;


public class CoffeeMaschine extends Agent {
	String Conversation1;
	protected void setup() {
		// description
		ServiceDescription serviceDescription = new ServiceDescription();
		serviceDescription.setName("tea");
		serviceDescription.setName("coffee with milk");
		serviceDescription.setName("coffee");
		serviceDescription.setType("Coffee-machine");
		// agent
		DFAgentDescription agentDescription = new DFAgentDescription();
		agentDescription.setName(getAID());
		agentDescription.addServices(serviceDescription);
		try {
			// register DF
			DFService.register(this, agentDescription);
		} catch (FIPAException exception) {
			exception.printStackTrace();
		}
		MessageTemplate template = AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_REQUEST);
		addBehaviour(new RespondToWaitress(this, template));
	}

	public class RespondToWaitress extends AchieveREResponder {

		public RespondToWaitress(CoffeeMaschine coffeeMaschine, MessageTemplate mt) {
			super(coffeeMaschine, mt);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
			// send AGREE
			Conversation1 = request.getContent();
			ACLMessage agree = request.createReply();
			agree.setPerformative(ACLMessage.AGREE);
			System.out.println("CoffeeMaschine: My task is to do the "+ Conversation1.toString());
			return agree;
			// send REFUSE
			// throw new RefuseException("check-failed");
		}
		@Override
		protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
			ACLMessage inform = request.createReply();
			System.out.println("CoffeeMaschine: "+ Conversation1+" is done! Give Waitress...");
				inform.setContent(Conversation1);
				inform.setPerformative(ACLMessage.INFORM);

			return inform;
		}
		private static final long serialVersionUID = -8009542545033008746L;
	}
	
	public ACLMessage starterMessage;

	class Recipes{
		public Map<String, List<String>> menu = new HashMap<String, List<String>>();
		List<String> CoffeeWithMilkList = new ArrayList<String>();
		List<String> CoffeeList = new ArrayList<String>();
		List<String> TeaList = new ArrayList<String>();
		
		public Recipes () {
			CoffeeWithMilkList.add("boiling water");
			CoffeeWithMilkList.add("grinding beans");
			CoffeeWithMilkList.add("bringing milk");
			menu.put("CoffeeWithMilk", CoffeeWithMilkList);
			CoffeeList.add("boiling water");
			CoffeeList.add("grinding beans");
			menu.put("Coffee", CoffeeList);
			TeaList.add("boiling water");
			TeaList.add("bringing tea leaves");
			menu.put("Tea", TeaList);
		}
		
		public Map<String, List<String>> getRecipe() {
			return menu;
		}
	}

	class ExecuteOrders extends TickerBehaviour {
		private static final long serialVersionUID = -1534610326024914625L;
		
		public String obj;
		public int recipeLen;
		public int cnt = 0;

		public ExecuteOrders(Agent a, long period, ACLMessage msg) {
			super(a, period);
			obj = msg.getContent();
		}

		@Override
		protected void onTick() {
			System.out.println("\nLooking for robots to make " + obj);
			Recipes menu = new Recipes();
			List<String> recipe = menu.getRecipe().get(obj);
			
			recipeLen = recipe.size();
			cnt = 0;
			
			for (String ing : recipe) {
				System.out.println("Looking for robots for " + ing);				
				List<AID> agents = findAgents(ing);
				if (!agents.isEmpty()) {
					System.out.println("Robots are found. Their names:");
					for (AID ag : agents) {
						System.out.println(ag.getLocalName());
					}
					System.out.println("Starting to make " + obj);
					
					String requestedAction = "work!";
					ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
					msg.setConversationId(ing);
					msg.addReceiver(agents.get(0));
					msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
					msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
					msg.setContent(requestedAction);

					addBehaviour(new RequestToExecute(myAgent, msg));
				} else {
					System.out.println("No robots for " + ing + " are found");
				}
			}
		}
		
		@Override
		public void stop() {
			System.out.println(obj + " is done");
			super.stop();
		}

		private List<AID> findAgents(String serviceName) {
			ServiceDescription requiredService = new ServiceDescription();
			requiredService.setName(serviceName);
			DFAgentDescription agentDescriptionTemplate = new DFAgentDescription();
			agentDescriptionTemplate.addServices(requiredService);

			List<AID> foundAgents = new ArrayList<AID>();
			try {
				DFAgentDescription[] agentDescriptions = DFService.search(myAgent, agentDescriptionTemplate);
				for (DFAgentDescription agentDescription : agentDescriptions) {
					foundAgents.add(agentDescription.getName());
				}
			} catch (FIPAException exception) {
				exception.printStackTrace();
			}

			return foundAgents;
		}

		class RequestToExecute extends AchieveREInitiator {
			private static final long serialVersionUID = -8104498062148279796L;
			
			public RequestToExecute(Agent a, ACLMessage msg) {
				super(a, msg);
			}

			@Override
			protected void handleInform(ACLMessage inform) {
				System.out.println(inform.getContent());
				cnt += 1;
				if (cnt == recipeLen) {
					stop();

					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					msg.addReceiver(starterMessage.getSender());
					msg.setContent(starterMessage.getContent());
					send(msg);
				}
			}
		}
	}

}
