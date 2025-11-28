//package examples.bookTrading;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.lang.acl.ACLMessage;

public class GUIAgent extends Agent {

    protected void setup() {
        System.out.println(getLocalName() + " started (GUIAgent)");

        Object[] args = getArguments();
        if (args != null && args.length >= 1) {
            String command = (String) args[0];

            if (command.equals("create-auction") && args.length >= 3) {
                String title = (String) args[1];
                String price = (String) args[2];
                createAuction(title, price);
            } else if (command.equals("start-bid") && args.length >= 4) {
                String item = (String) args[1];
                String offer = (String) args[2];
                String auctionAgentName = (String) args[3];
                startBidding(item, offer, auctionAgentName);
            } else {
                System.out.println("Unknown command or insufficient arguments.");
            }
        } else {
            System.out.println("No command provided to GUIAgent.");
        }
    }

    private void createAuction(String title, String price) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setConversationId("book-auction-create");
                msg.setContent(title + ";" + price);
                msg.addReceiver(new AID("Main", AID.ISLOCALNAME));
                send(msg);
                System.out.println("Sent auction creation request to MainAgent.");
            }
        });
    }

    private void startBidding(String item, String offer, String auctionAgentName) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                try {
                    ContainerController container = getContainerController();
                    AgentController biddingAgent = container.createNewAgent(
                            "Bidder-" + item + "-" + System.currentTimeMillis(),
                            "examples.bookTrading.BiddingAgent",
                            new Object[] { item, offer, auctionAgentName }
                    );
                    biddingAgent.start();
                    System.out.println("Started BiddingAgent for " + item);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
