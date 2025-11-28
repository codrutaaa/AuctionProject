//package examples.bookTrading;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class BiddingAgent extends Agent {
    protected void setup() {
        System.out.println(getLocalName() + " started (BiddingAgent).");

        Object[] args = getArguments();
        if (args != null && args.length == 3) {
            String item = (String) args[0];
            int offerPrice = Integer.parseInt((String) args[1]);
            AID auctionAgent = new AID((String) args[2], AID.ISLOCALNAME);

            addBehaviour(new OneShotBehaviour() {
                public void action() {
                    ACLMessage bid = new ACLMessage(ACLMessage.PROPOSE);
                    bid.addReceiver(auctionAgent);
                    bid.setConversationId("book-auction");
                    bid.setContent(item + ";" + offerPrice);
                    send(bid);
                    System.out.println("Bid sent for item: " + item + ", price: " + offerPrice);
                }
            });
        } else {
            System.out.println("Missing arguments: [itemTitle, price, auctionAgentName]");
            doDelete();
        }
    }
}
