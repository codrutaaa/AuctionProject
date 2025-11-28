//package examples.bookTrading;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;

import java.util.*;

public class AuctionAgent extends Agent {
    private Map<String, Auction> auctions = new HashMap<>();

    protected void setup() {
        System.out.println(getLocalName() + " started.");
        registerAuctionService();
        addBehaviour(new AuctionManager());
        addBehaviour(new BidReceiver());
    }

    private void registerAuctionService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("auction-service");
        sd.setName("book-auction");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private class Auction {
        String title;
        int minPrice;
        AID seller;
        AID bestBidder = null;
        int bestPrice = 0;
        List<AID> participants = new ArrayList<>();

        Auction(String title, int minPrice, AID seller) {
            this.title = title;
            this.minPrice = minPrice;
            this.seller = seller;
        }

        void registerParticipant(AID bidder) {
            if (!participants.contains(bidder)) {
                participants.add(bidder);
            }
        }

        void notifyAllBidders(String message) {
            ACLMessage notify = new ACLMessage(ACLMessage.INFORM);
            for (AID p : participants) {
                notify.addReceiver(p);
            }
            notify.setContent(message);
            notify.setConversationId("auction-update");
            send(notify);
        }
    }

    // Primesc solicitarea de creare licitatie: "title;price"
    private class AuctionManager extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive(MessageTemplate.MatchConversationId("book-auction-create"));
            if (msg != null) {
                String[] parts = msg.getContent().split(";");
                String title = parts[0];
                int price = Integer.parseInt(parts[1]);

                Auction auction = new Auction(title, price, msg.getSender());
                auctions.put(title, auction);

                System.out.println("Auction created for: " + title);
                sendCFP(title);

                // Închide licitația după 30 secunde
                addBehaviour(new WakerBehaviour(myAgent, 30000) {
                    protected void onWake() {
                        endAuction(title);
                    }
                });
            } else {
                block();
            }
        }

        private void sendCFP(String title) {
            try {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("book-buying");
                template.addServices(sd);
                DFAgentDescription[] result = DFService.search(myAgent, template);

                ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                for (DFAgentDescription dfd : result) {
                    cfp.addReceiver(dfd.getName());
                }
                cfp.setConversationId("book-auction");
                cfp.setContent(title);
                send(cfp);
                System.out.println("CFP sent for auction: " + title);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void endAuction(String title) {
            Auction auction = auctions.get(title);
            if (auction != null) {
                String result = auction.bestBidder != null
                        ? "Auction ended. Winner: " + auction.bestBidder.getLocalName() + ", price: " + auction.bestPrice
                        : "Auction ended. No bids received.";
                System.out.println(result);

                // Trimite rezultatul la vânzător și la câștigător
                ACLMessage end = new ACLMessage(ACLMessage.INFORM);
                if (auction.bestBidder != null) {
                    end.addReceiver(auction.bestBidder);
                }
                end.addReceiver(auction.seller);
                end.setContent(result);
                end.setConversationId("auction-result");
                send(end);

                auctions.remove(title);
            }
        }
    }

    // Primește biduri: "title;price"
    private class BidReceiver extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
            if (msg != null && "book-auction".equals(msg.getConversationId())) {
                String[] parts = msg.getContent().split(";");
                String title = parts[0];
                int offer = Integer.parseInt(parts[1]);

                Auction auction = auctions.get(title);
                if (auction != null) {
                    auction.registerParticipant(msg.getSender());

                    if (offer > auction.bestPrice) {
                        auction.bestPrice = offer;
                        auction.bestBidder = msg.getSender();
                        System.out.println("New best offer for " + title + ": " + offer);

                        auction.notifyAllBidders("New highest bid: " + offer + " for " + title);
                    }
                }
            } else {
                block();
            }
        }
    }
}
