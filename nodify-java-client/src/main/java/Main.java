import com.itexpert.jclient.NodifyClient;

public class Main {

        public static void main(String[] args) {

            // 1️⃣ Initialisation du client via builder
            NodifyClient nodifyClient = NodifyClient.builder()
                    .baseUrl("https://api.nodify.io")
                    .apiKey("YOUR_API_KEY")
                    .build();

            // 2️⃣ Utilisation du flow fluent
            nodifyClient
                    .checkIfNodeExist("CODE_NODE_123")
                    .ifExists(node -> {
                        System.out.println("Node exists: " + node.getCode());
                        // Mise à jour du node
                        node.setTitle("Titre mis à jour");
                        node.setContent("Contenu mis à jour via Fluent API");
                    })
                    .ifNotExists(() -> {
                        System.out.println("Node does not exist, creating a new one...");
                        // Ici tu pourrais créer un NodeDto et le passer au save si besoin
                    })
                    .save()      // Sauvegarde du node
                    .publish();  // Publication du node

            System.out.println("Process completed successfully.");
    }
}
