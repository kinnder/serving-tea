package coffeeMachine;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import commonFunctionality.CommonAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
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

public class CoffeeMachine extends CommonAgent {
	@Override
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

		// adding behaviours

		MessageTemplate reqTemp = AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_REQUEST);

		addBehaviour(new WaitingOrders(this, reqTemp));
	}
	
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

	class WaitingOrders extends AchieveREResponder {
		private static final long serialVersionUID = 6130496380982287815L;

		public WaitingOrders(Agent a, MessageTemplate mt) {
			super(a, mt);
		}

		@Override
		protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
			// send AGREE
			ACLMessage agree = request.createReply();
			agree.setContent(request.getContent());
			agree.setPerformative(ACLMessage.AGREE);
			System.out.println("agree " + agree.getContent());

			addBehaviour(new ExecuteOrders(myAgent, 2000, agree));
			return agree;
			// send REFUSE
			// throw new RefuseException("check-failed");
		}
	}

	class ExecuteOrders extends TickerBehaviour {
		private static final long serialVersionUID = -1534610326024914625L;
		
		public ACLMessage msg;

		public ExecuteOrders(Agent a, long period, ACLMessage _msg) {
			super(a, period);
			msg = _msg;
		}

		@Override
		protected void onTick() {
			System.out.println("\nlooking for agents with printing service");
			Recipes menu = new Recipes();
			List<String> recipe = menu.getRecipe().get(msg.getContent());
			for (String ing : recipe) {
				System.out.println(ing);				
				List<AID> agents = findAgents(ing);
				if (!agents.isEmpty()) {
					System.out.println("robots are found. starting to make " + msg.getContent());
					String requestedAction = "work!";
					ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
					msg.addReceiver(agents.get(0));
					msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
					msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
					msg.setContent(requestedAction);

					addBehaviour(new RequestToExecute(myAgent, msg));
				} else {
					System.out.println("no robots for" + ing + "are found");
				}
			}			
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
			public RequestToExecute(Agent a, ACLMessage msg) {
				super(a, msg);
			}

			@Override
			protected void handleRefuse(ACLMessage refuse) {
				System.out.println("received refuse");
			}

			@Override
			protected void handleAgree(ACLMessage agree) {
				System.out.println("received agree");
			}

			@Override
			protected void handleInform(ACLMessage inform) {
				System.out.println("received inform");
			}

			@Override
			protected void handleFailure(ACLMessage failure) {
				System.out.println("received failure");
			}

			private static final long serialVersionUID = -8104498062148279796L;
		}
		
		private void sendMessage(ACLMessage msgToLog, String agentName, String protocol) {
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.addReceiver(new AID((agentName), AID.ISLOCALNAME));
			msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
			msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
			msg.setConversationId("machine");
			msg.setContent(msgToLog.getContent());

			send(msg);
		}
	}
}
