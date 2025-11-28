
//package examples.bookTrading;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

public class SearchAgent extends Agent {
    private String targetBook;
    private AID notifyAgent;  // Cine să fie notificat când se găsește

    protected void setup() {
        System.out.println(getLocalName() + " started (SearchAgent)");

        Object[] args = getArguments();
        if (args != null && args.length >= 1) {
            targetBook = (String) args[0];
            notifyAgent = (args.length >= 2) ? new AID((String) args[1], AID.ISLOCALNAME) : null;

            addBehaviour(new TickerBehaviour(this, 10000) { // la fiecare 10 secunde
                protected void onTick() {
                    System.out.println("Searching for auctions with: " + targetBook);
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("auction-service");
                    template.addServices(sd);

                    try {
                        DFAgentDescription[] result = DFService.search(myAgent, template);
                        for (DFAgentDescription dfd : result) {
                            AID auctionAgent = dfd.getName();

                            // Trimite mesaj către agentul notificat
                            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                            msg.setConversationId("search-result");
                            msg.setContent("Auction found for: " + targetBook + " at agent: " + auctionAgent.getLocalName());

                            if (notifyAgent != null) {
                                msg.addReceiver(notifyAgent);
                            } else {
                                msg.addReceiver(getAID());  // trimite către sine
                            }

                            send(msg);
                        }
                    } catch (FIPAException fe) {
                        fe.printStackTrace();
                    }
                }
            });

        } else {
            System.out.println("No book title provided. Agent terminating.");
            doDelete();
        }
    }
}
