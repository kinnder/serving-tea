package Robots;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

public class Robot_Agent extends Agent {

private static final long serialVersionUID = -7418692714860762106L;

	@Override
	protected void setup() {
		ServiceDescription serviceDescription = new ServiceDescription();
		serviceDescription.setName("boiling water");
		serviceDescription.setType("Coffee-machine");
		DFAgentDescription agentDescription = new DFAgentDescription();
		agentDescription.setName(getAID());
		agentDescription.addServices(serviceDescription);
		try {
			DFService.register(this, agentDescription);
		} catch (FIPAException exception) {
			exception.printStackTrace();
		}

		MessageTemplate template = AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_REQUEST);
		addBehaviour(new RobotResponder(this, template));
	}

	@Override
	protected void takeDown() {
		try {
			DFService.deregister(this);
		} catch (FIPAException exception) {
			exception.printStackTrace();
		}
	}


	class RobotResponder extends AchieveREResponder {
		public RobotResponder(Agent a, MessageTemplate mt) {
			super(a, mt);
		}

		@Override
		protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response)
				throws FailureException {
			System.out.println("hehe");
			ACLMessage inform = request.createReply();
			inform.setContent("boiled!");
			inform.setPerformative(ACLMessage.INFORM);
			return inform;
		}

		private static final long serialVersionUID = -8009542545033008746L;
	}

}