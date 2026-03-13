// exemple-utilisation.ts

import { NodifyClient } from "./nodify-client";

// Création du client avec authentification
async function createAndUseClient() {
  try {
    // 1. Création du client
    const client = NodifyClient.builder()
      .withBaseUrl('http://localhost:8080')
      .withTimeout(15000)
      .withHeader('X-Custom-Header', 'valeur')
      .withAuthErrorHandler(async () => {
        // Handler pour rafraîchir le token
        console.log('Token expiré, tentative de rafraîchissement...');
        // Ici vous pourriez appeler un endpoint de refresh token
        return null; // Retourne null si pas de rafraîchissement possible
      })
      .build();

    // 2. Authentification
    const authResponse = await client.login('admin@example.com', 'password123');
    console.log('Authentifié avec token:', authResponse.token?.substring(0, 20) + '...');

    // 3. Vérification de la santé
    const health = await client.health();
    console.log('Health status:', health);

    // 4. Récupérer tous les nodes
    const nodes = await client.findAllNodes();
    console.log('Nombre de nodes:', nodes.length);

    // 5. Créer un nouveau node
    const newNode: Node = {
      name: 'Mon Nouveau Node',
      code: 'NODE-' + Date.now(),
      slug: 'mon-nouveau-node',
      environmentCode: 'default',
      defaultLanguage: 'fr',
      type: 'CONTENT',
      status: NodeStatus.NEW
    };

    const savedNode = await client.saveNode(newNode);
    console.log('Node créé:', savedNode);

    // 6. Récupérer les nodes publiés
    const publishedNodes = await client.findPublishedNodes();
    console.log('Nodes publiés:', publishedNodes.length);

    // 7. Créer un content node
    const newContent: ContentNode = {
      code: 'CONTENT-' + Date.now(),
      slug: 'mon-contenu',
      environmentCode: 'default',
      language: 'fr',
      type: 'HTML',
      title: 'Mon Premier Contenu',
      content: '<h1>Hello World</h1>',
      status: NodeStatus.NEW
    };

    const savedContent = await client.saveContentNode(newContent);
    console.log('Content node créé:', savedContent);

    // 8. Publier le content node
    const publishedContent = await client.publishContentNode(savedContent.code!, true);
    console.log('Content node publié:', publishedContent.status);

    // 9. Récupérer les statistiques
    const charts = await client.getCharts();
    console.log('Charts:', charts);

    // 10. Gérer les locks
    await client.acquireLock(savedNode.code!);
    console.log('Lock acquis pour:', savedNode.code);

    const lockInfo = await client.getLockOwner(savedNode.code!);
    console.log('Info lock:', lockInfo);

    await client.releaseLock(savedNode.code!);
    console.log('Lock libéré');

    // 11. Exporter un node
    const exportData = await client.exportAllNodes(savedNode.code!);
    console.log('Données exportées (taille):', exportData.length);

    // 12. Gérer les plugins
    const plugins = await client.findNotDeletedPlugins();
    console.log('Plugins disponibles:', plugins.length);

    // 13. Gérer les utilisateurs
    const users = await client.findAllUsers();
    console.log('Utilisateurs:', users.length);

    // 14. Gérer les feedbacks
    const feedbacks = await client.findAllFeedback();
    console.log('Feedbacks:', feedbacks.length);

    // 15. Nettoyage (optionnel)
    // await client.deleteNode(savedNode.code!);
    // console.log('Node supprimé');

  } catch (error) {
    console.error('Erreur:', error);
  }
}

// Exemple avec gestion d'erreurs détaillée
async function exampleWithErrorHandling() {
  const client = NodifyClient.builder()
    .withBaseUrl('http://localhost:8080')
    .build();

  try {
    // Essayer de faire une opération sans être authentifié
    await client.findAllNodes();
  } catch (error: any) {
    if (error.status === 401) {
      console.log('Non authentifié, tentative de login...');
      
      // Authentification
      try {
        await client.login('user@example.com', 'password');
        
        // Retry l'opération
        const nodes = await client.findAllNodes();
        console.log('Opération réussie après login');
      } catch (loginError) {
        console.error('Échec de l\'authentification:', loginError);
      }
    } else {
      console.error('Autre erreur:', error);
    }
  }
}

// Exemple avec pagination
async function exampleWithPagination() {
  const client = NodifyClient.builder()
    .withBaseUrl('http://localhost:8080')
    .build();

  await client.login('admin@example.com', 'password');

  // Récupérer les données avec pagination
  const data = await client.findDataByContentCode('CONTENT-123', {
    currentPage: 0,
    limit: 20
  });
  
  console.log('Données paginées:', data.length);
}

// Exemple d'import/export
async function exampleImportExport() {
  const client = NodifyClient.builder()
    .withBaseUrl('http://localhost:8080')
    .build();

  await client.login('admin@example.com', 'password');

  // Exporter un plugin
  const pluginId = '123e4567-e89b-12d3-a456-426614174000';
  const pluginExport = await client.exportPlugin(pluginId);
  
  // Sauvegarder le fichier exporté
  // fs.writeFileSync('plugin-export.zip', Buffer.from(pluginExport));
  console.log('Plugin exporté, taille:', pluginExport.length);

  // Importer des nodes
  const nodesToImport: Node[] = [
    {
      name: 'Node 1',
      code: 'IMPORT-1',
      slug: 'node-1',
      environmentCode: 'default',
      defaultLanguage: 'fr',
      type: 'CONTENT'
    },
    {
      name: 'Node 2',
      code: 'IMPORT-2',
      slug: 'node-2',
      environmentCode: 'default',
      defaultLanguage: 'fr',
      type: 'CONTENT'
    }
  ];

  const importedNodes = await client.importNodes(nodesToImport, 'PARENT-CODE');
  console.log('Nodes importés:', importedNodes.length);
}

// Lancer les exemples
createAndUseClient();