
//package examples.bookTrading;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class MainAgent extends Agent {

    protected void setup() {
        System.out.println(getLocalName() + " started (MainAgent)");

        Object[] args = getArguments();
        if (args != null && args.length >= 2) {
            String auctionTitle = (String) args[0];
            int minPrice = Integer.parseInt((String) args[1]);

            addBehaviour(new OneShotBehaviour() {
                public void action() {
                    try {
                        // Creăm un nou AuctionAgent cu parametrii primiți
                        jade.wrapper.AgentContainer container = getContainerController();
                        jade.wrapper.AgentController auctionAgent = container.createNewAgent(
                                "auction-" + auctionTitle.replaceAll("\\s+", "_"),
                                "examples.bookTrading.AuctionAgent",
                                null
                        );
                        auctionAgent.start();

                        // Trimitem mesaj de inițializare cu titlul și prețul minim
                        ACLMessage createAuction = new ACLMessage(ACLMessage.INFORM);
                        createAuction.setConversationId("book-auction-create");
                        createAuction.setContent(auctionTitle + ";" + minPrice);
                        createAuction.addReceiver(new AID("auction-" + auctionTitle.replaceAll("\\s+", "_"), AID.ISLOCALNAME));
                        send(createAuction);

                        System.out.println("AuctionAgent created for auction: " + auctionTitle);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            System.out.println("No auction title or price provided.");
            doDelete();
        }
    }
}
