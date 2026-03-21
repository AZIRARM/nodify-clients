import { NodifyClient, NodeStatus, ContentNodeType, ContentNode, Node, Translation, Value } from 'nodify-node-client';

interface Article {
  id?: string;
  code: string;
  titleKey: string;
  descriptionKey: string;
  contentKey: string;
  author: string;
  date: string;
  imageCode?: string;
  tags: string[];
  translations?: Translation[];
}

interface Feature {
  id?: string;
  code: string;
  titleKey: string;
  descriptionKey: string;
  imageCode?: string;
  translations?: Translation[];
}

interface ImageContent {
  id?: string;
  code: string;
  name: string;
  alt: string;
  url: string;
  width?: number;
  height?: number;
}

interface NodifyConfig {
  coreUrl: string;
  apiUrl: string;
}

class LandingPageBuilder {
  private client: NodifyClient;
  private config: NodifyConfig;
  private siteNode: Node | null = null;
  private articlesNode: Node | null = null;
  private picturesNode: Node | null = null;
  private featuresNode: Node | null = null;
  private styleNode: ContentNode | null = null;
  private scriptNode: ContentNode | null = null;
  private articles: Article[] = [];
  private features: Feature[] = [];
  private images: Map<string, ImageContent> = new Map();
  private landingPageNode: ContentNode | null = null;
  private mainTranslations: Translation[] = [];

  constructor(config?: Partial<NodifyConfig>) {
    this.config = {
      coreUrl: config?.coreUrl || 'http://localhost:8080',
      apiUrl: config?.apiUrl || 'http://localhost:1080'
    };
    
    this.client = NodifyClient.builder()
      .withBaseUrl(this.config.coreUrl)
      .withTimeout(30000)
      .build();
    
    this.initTranslations();
  }

  private initTranslations(): void {
    this.mainTranslations = [
      // EN translations
      { key: 'MAIN_TITLE', language: 'EN', value: 'Nodify - Modern Headless CMS' },
      { key: 'MAIN_SUBTITLE', language: 'EN', value: 'A powerful Headless CMS for modern web applications' },
      { key: 'NAV_HOME', language: 'EN', value: 'Home' },
      { key: 'NAV_FEATURES', language: 'EN', value: 'Features' },
      { key: 'NAV_ARTICLES', language: 'EN', value: 'Articles' },
      { key: 'NAV_CONTACT', language: 'EN', value: 'Contact' },
      { key: 'ARTICLES_TITLE', language: 'EN', value: '📖 Latest Articles' },
      { key: 'FEATURES_TITLE', language: 'EN', value: '✨ Key Features' },
      { key: 'FOOTER_TEXT', language: 'EN', value: 'All rights reserved.' },
      { key: 'FOOTER_BUILT', language: 'EN', value: 'Built with ❤️ using' },
      { key: 'READ_MORE', language: 'EN', value: 'Read More →' },
      { key: 'BY', language: 'EN', value: 'By' },
      { key: 'TAGS', language: 'EN', value: '🏷️ Tags:' },
      
      // FR translations
      { key: 'MAIN_TITLE', language: 'FR', value: 'Nodify - CMS Headless Moderne' },
      { key: 'MAIN_SUBTITLE', language: 'FR', value: 'Un CMS Headless puissant pour les applications web modernes' },
      { key: 'NAV_HOME', language: 'FR', value: 'Accueil' },
      { key: 'NAV_FEATURES', language: 'FR', value: 'Fonctionnalités' },
      { key: 'NAV_ARTICLES', language: 'FR', value: 'Articles' },
      { key: 'NAV_CONTACT', language: 'FR', value: 'Contact' },
      { key: 'ARTICLES_TITLE', language: 'FR', value: '📖 Derniers Articles' },
      { key: 'FEATURES_TITLE', language: 'FR', value: '✨ Fonctionnalités Clés' },
      { key: 'FOOTER_TEXT', language: 'FR', value: 'Tous droits réservés.' },
      { key: 'FOOTER_BUILT', language: 'FR', value: 'Construit avec ❤️ en utilisant' },
      { key: 'READ_MORE', language: 'FR', value: 'Lire la suite →' },
      { key: 'BY', language: 'FR', value: 'Par' },
      { key: 'TAGS', language: 'FR', value: '🏷️ Tags:' },
      
      // ES translations
      { key: 'MAIN_TITLE', language: 'ES', value: 'Nodify - CMS Headless Moderno' },
      { key: 'MAIN_SUBTITLE', language: 'ES', value: 'Un CMS Headless potente para aplicaciones web modernas' },
      { key: 'NAV_HOME', language: 'ES', value: 'Inicio' },
      { key: 'NAV_FEATURES', language: 'ES', value: 'Características' },
      { key: 'NAV_ARTICLES', language: 'ES', value: 'Artículos' },
      { key: 'NAV_CONTACT', language: 'ES', value: 'Contacto' },
      { key: 'ARTICLES_TITLE', language: 'ES', value: '📖 Últimos Artículos' },
      { key: 'FEATURES_TITLE', language: 'ES', value: '✨ Características Clave' },
      { key: 'FOOTER_TEXT', language: 'ES', value: 'Todos los derechos reservados.' },
      { key: 'FOOTER_BUILT', language: 'ES', value: 'Construido con ❤️ usando' },
      { key: 'READ_MORE', language: 'ES', value: 'Leer más →' },
      { key: 'BY', language: 'ES', value: 'Por' },
      { key: 'TAGS', language: 'ES', value: '🏷️ Etiquetas:' }
    ];
  }

  async build(): Promise<void> {
    try {
      console.log('\n╔════════════════════════════════════════════════════════════╗');
      console.log('║     Nodify Landing Page Builder - Node.js Client           ║');
      console.log('╚════════════════════════════════════════════════════════════╝\n');

      await this.login();
      
      await this.createSiteNode();
      await this.createArticlesNode();
      await this.createPicturesNode();
      await this.createFeaturesNode();
      await this.createImages();
      await this.createFeatures();
      await this.createArticles();
      await this.createStyleNode();
      await this.createScriptNode();
      await this.createLandingPage();
      await this.publishSite();
      
      this.displayResults();
      
    } catch (error) {
      console.error('❌ Error building landing page:', error);
      throw error;
    }
  }

  private async login(): Promise<void> {
    console.log('🔐 Authenticating...');
    await this.client.login('admin', 'Admin13579++');
    console.log('✅ Authenticated successfully\n');
  }

  private generateCode(prefix: string): string {
    return prefix + '-' + Math.random().toString(36).substring(2, 10).toUpperCase();
  }

  private async createSiteNode(): Promise<void> {
    console.log('📁 Creating main site node...');
    
    const siteNode: Node = {
      name: 'Nodify Landing Page',
      code: this.generateCode('SITE'),
      type: 'SITE',
      status: NodeStatus.SNAPSHOT,
      defaultLanguage: 'EN',
      description: 'A beautiful landing page built with Nodify',
      languages: ['EN', 'FR', 'ES']
    };
    
    this.siteNode = await this.client.saveNode(siteNode);
    console.log(`✅ Main site node created: ${this.siteNode.code}\n`);
  }

  private async createArticlesNode(): Promise<void> {
    console.log('📁 Creating "articles" sub-node...');
    
    const articlesNode: Node = {
      parentCode: this.siteNode!.code,
      name: 'Articles',
      code: this.generateCode('ARTICLES'),
      type: 'FOLDER',
      status: NodeStatus.SNAPSHOT,
      description: 'Container for all blog articles'
    };
    
    this.articlesNode = await this.client.saveNode(articlesNode);
    console.log(`✅ Articles node created: ${this.articlesNode.code}\n`);
  }

  private async createPicturesNode(): Promise<void> {
    console.log('📁 Creating "pictures" sub-node...');
    
    const picturesNode: Node = {
      parentCode: this.siteNode!.code,
      name: 'Pictures',
      code: this.generateCode('PICTURES'),
      type: 'FOLDER',
      status: NodeStatus.SNAPSHOT,
      description: 'Container for all images'
    };
    
    this.picturesNode = await this.client.saveNode(picturesNode);
    console.log(`✅ Pictures node created: ${this.picturesNode.code}\n`);
  }

  private async createFeaturesNode(): Promise<void> {
    console.log('📁 Creating "features" sub-node...');
    
    const featuresNode: Node = {
      parentCode: this.siteNode!.code,
      name: 'Features',
      code: this.generateCode('FEATURES'),
      type: 'FOLDER',
      status: NodeStatus.SNAPSHOT,
      description: 'Container for all features'
    };
    
    this.featuresNode = await this.client.saveNode(featuresNode);
    console.log(`✅ Features node created: ${this.featuresNode.code}\n`);
  }

  private async createImages(): Promise<void> {
    console.log('🖼️ Creating images under "pictures" node...');
    
    const images = [
      { name: 'Hero Image', alt: 'Nodify CMS Hero', url: 'https://picsum.photos/id/1/1200/600', width: 1200, height: 600 },
      { name: 'Feature 1 Image', alt: 'Headless CMS Feature', url: 'https://picsum.photos/id/2/400/300', width: 400, height: 300 },
      { name: 'Feature 2 Image', alt: 'API First', url: 'https://picsum.photos/id/3/400/300', width: 400, height: 300 },
      { name: 'Feature 3 Image', alt: 'Multi-language', url: 'https://picsum.photos/id/4/400/300', width: 400, height: 300 }
    ];
    
    for (const img of images) {
      const imageCode = this.generateCode('IMG');
      const imageContent: ContentNode = {
        parentCode: this.picturesNode!.code,
        type: ContentNodeType.PICTURE,
        title: img.name,
        code: imageCode,
        language: 'EN',
        status: NodeStatus.SNAPSHOT,
        environmentCode: 'production',
        description: img.alt,
        content: img.url,
        values: [
          { key: 'WIDTH', value: img.width?.toString() || '' },
          { key: 'HEIGHT', value: img.height?.toString() || '' },
          { key: 'ALT', value: img.alt }
        ]
      };
      
      const saved = await this.client.saveContentNode(imageContent);
      this.images.set(img.name, {
        id: saved.code,
        code: saved.code!,
        name: img.name,
        alt: img.alt,
        url: img.url,
        width: img.width,
        height: img.height
      });
      console.log(`  ✅ Image created: ${img.name} (${saved.code})`);
    }
    console.log('');
  }

  private async createFeatures(): Promise<void> {
    console.log('📋 Creating features under "features" node with $translate() directives...');
    
    const featuresData = [
      {
        code: this.generateCode('FEAT'),
        titleKey: 'FEATURE_1_TITLE',
        descriptionKey: 'FEATURE_1_DESC',
        imageName: 'Feature 1 Image'
      },
      {
        code: this.generateCode('FEAT'),
        titleKey: 'FEATURE_2_TITLE',
        descriptionKey: 'FEATURE_2_DESC',
        imageName: 'Feature 2 Image'
      },
      {
        code: this.generateCode('FEAT'),
        titleKey: 'FEATURE_3_TITLE',
        descriptionKey: 'FEATURE_3_DESC',
        imageName: 'Feature 3 Image'
      }
    ];
    
    // Feature translations
    const featureTranslations: Translation[] = [
      // Feature 1
      { key: 'FEATURE_1_TITLE', language: 'EN', value: '🚀 Headless Architecture' },
      { key: 'FEATURE_1_DESC', language: 'EN', value: 'Separate content management from presentation for ultimate flexibility and scalability' },
      { key: 'FEATURE_1_TITLE', language: 'FR', value: '🚀 Architecture Headless' },
      { key: 'FEATURE_1_DESC', language: 'FR', value: 'Séparez la gestion de contenu de la présentation pour une flexibilité et une évolutivité ultimes' },
      { key: 'FEATURE_1_TITLE', language: 'ES', value: '🚀 Arquitectura Headless' },
      { key: 'FEATURE_1_DESC', language: 'ES', value: 'Separe la gestión de contenido de la presentación para una flexibilidad y escalabilidad óptimas' },
      
      // Feature 2
      { key: 'FEATURE_2_TITLE', language: 'EN', value: '⚡ API-First Design' },
      { key: 'FEATURE_2_DESC', language: 'EN', value: 'RESTful APIs with powerful querying capabilities and real-time updates' },
      { key: 'FEATURE_2_TITLE', language: 'FR', value: '⚡ Design API-First' },
      { key: 'FEATURE_2_DESC', language: 'FR', value: 'API RESTful avec des capacités de requêtage puissantes et des mises à jour en temps réel' },
      { key: 'FEATURE_2_TITLE', language: 'ES', value: '⚡ Diseño API-First' },
      { key: 'FEATURE_2_DESC', language: 'ES', value: 'API RESTful con capacidades de consulta potentes y actualizaciones en tiempo real' },
      
      // Feature 3
      { key: 'FEATURE_3_TITLE', language: 'EN', value: '🌍 Multi-language Support' },
      { key: 'FEATURE_3_DESC', language: 'EN', value: 'Built-in internationalization for reaching global audiences' },
      { key: 'FEATURE_3_TITLE', language: 'FR', value: '🌍 Support Multilingue' },
      { key: 'FEATURE_3_DESC', language: 'FR', value: 'Internationalisation intégrée pour atteindre un public mondial' },
      { key: 'FEATURE_3_TITLE', language: 'ES', value: '🌍 Soporte Multilingüe' },
      { key: 'FEATURE_3_DESC', language: 'ES', value: 'Internacionalización integrada para llegar a audiencias globales' }
    ];
    
    for (const feature of featuresData) {
      const image = this.images.get(feature.imageName);
      const jsonContent = JSON.stringify({
        title: `$translate(${feature.titleKey})`,
        description: `$translate(${feature.descriptionKey})`,
        imageCode: image?.code
      });
      
      const featureTranslationsForThis = featureTranslations.filter(t => 
        t.key === feature.titleKey || t.key === feature.descriptionKey
      );
      
      const featureNode: ContentNode = {
        parentCode: this.featuresNode!.code,
        type: ContentNodeType.JSON,
        title: feature.code,
        code: feature.code,
        language: 'EN',
        status: NodeStatus.SNAPSHOT,
        environmentCode: 'production',
        description: `$translate(${feature.descriptionKey})`,
        content: jsonContent,
        translations: featureTranslationsForThis
      };
      
      const saved = await this.client.saveContentNode(featureNode);
      this.features.push({ 
        id: saved.code,
        code: feature.code,
        titleKey: feature.titleKey,
        descriptionKey: feature.descriptionKey,
        imageCode: image?.code,
        translations: featureTranslationsForThis
      });
      console.log(`  ✅ Feature created: ${feature.code}`);
    }
    console.log('');
  }

  private async createArticles(): Promise<void> {
    console.log('📝 Creating articles under "articles" node with $translate() directives...');
    
    const articlesData = [
      {
        code: this.generateCode('POST'),
        titleKey: 'ARTICLE_1_TITLE',
        descriptionKey: 'ARTICLE_1_DESC',
        contentKey: 'ARTICLE_1_CONTENT',
        author: 'Sarah Johnson',
        date: new Date('2024-01-15').toISOString(),
        imageName: 'Feature 1 Image',
        tags: ['headless', 'cms', 'getting-started']
      },
      {
        code: this.generateCode('POST'),
        titleKey: 'ARTICLE_2_TITLE',
        descriptionKey: 'ARTICLE_2_DESC',
        contentKey: 'ARTICLE_2_CONTENT',
        author: 'Michael Chen',
        date: new Date('2024-01-20').toISOString(),
        imageName: 'Feature 2 Image',
        tags: ['modern-web', 'scalability', 'api']
      },
      {
        code: this.generateCode('POST'),
        titleKey: 'ARTICLE_3_TITLE',
        descriptionKey: 'ARTICLE_3_DESC',
        contentKey: 'ARTICLE_3_CONTENT',
        author: 'Emily Rodriguez',
        date: new Date('2024-01-25').toISOString(),
        imageName: 'Feature 3 Image',
        tags: ['api-first', 'rest', 'development']
      }
    ];
    
    // Article translations
    const articleTranslations: Translation[] = [
      // Article 1
      { key: 'ARTICLE_1_TITLE', language: 'EN', value: 'Getting Started with Headless CMS' },
      { key: 'ARTICLE_1_DESC', language: 'EN', value: 'Learn how to use a headless CMS for your next project' },
      { key: 'ARTICLE_1_CONTENT', language: 'EN', value: '<h2>What is a Headless CMS?</h2><p>A headless CMS separates content management from presentation.</p><h2>Benefits</h2><ul><li>Flexibility: Use any frontend framework</li><li>Scalability: Serve content via APIs</li><li>Performance: Optimized content delivery</li></ul>' },
      { key: 'ARTICLE_1_TITLE', language: 'FR', value: 'Démarrer avec un CMS Headless' },
      { key: 'ARTICLE_1_DESC', language: 'FR', value: 'Apprenez à utiliser un CMS headless pour votre prochain projet' },
      { key: 'ARTICLE_1_CONTENT', language: 'FR', value: '<h2>Qu\'est-ce qu\'un CMS Headless ?</h2><p>Un CMS headless sépare la gestion de contenu de la présentation.</p><h2>Avantages</h2><ul><li>Flexibilité : Utilisez n\'importe quel framework frontend</li><li>Scalabilité : Servez le contenu via des APIs</li><li>Performance : Livraison de contenu optimisée</li></ul>' },
      { key: 'ARTICLE_1_TITLE', language: 'ES', value: 'Comenzando con CMS Headless' },
      { key: 'ARTICLE_1_DESC', language: 'ES', value: 'Aprenda a usar un CMS headless para su próximo proyecto' },
      { key: 'ARTICLE_1_CONTENT', language: 'ES', value: '<h2>¿Qué es un CMS Headless?</h2><p>Un CMS headless separa la gestión de contenido de la presentación.</p><h2>Beneficios</h2><ul><li>Flexibilidad: Use cualquier framework frontend</li><li>Escalabilidad: Sirva contenido a través de APIs</li><li>Rendimiento: Entrega de contenido optimizada</li></ul>' },
      
      // Article 2
      { key: 'ARTICLE_2_TITLE', language: 'EN', value: 'Building Modern Web Applications' },
      { key: 'ARTICLE_2_DESC', language: 'EN', value: 'How Nodify helps you build scalable applications' },
      { key: 'ARTICLE_2_CONTENT', language: 'EN', value: '<h2>Modern Web Development</h2><p>Nodify provides the perfect foundation for building modern applications.</p><h2>Key Features</h2><ul><li>RESTful API</li><li>Content Versioning</li><li>Workflow Management</li><li>Multi-language Support</li></ul>' },
      { key: 'ARTICLE_2_TITLE', language: 'FR', value: 'Construire des Applications Web Modernes' },
      { key: 'ARTICLE_2_DESC', language: 'FR', value: 'Comment Nodify vous aide à construire des applications évolutives' },
      { key: 'ARTICLE_2_CONTENT', language: 'FR', value: '<h2>Développement Web Moderne</h2><p>Nodify fournit la base parfaite pour construire des applications modernes.</p><h2>Fonctionnalités Clés</h2><ul><li>API RESTful</li><li>Versioning du Contenu</li><li>Gestion des Workflows</li><li>Support Multilingue</li></ul>' },
      { key: 'ARTICLE_2_TITLE', language: 'ES', value: 'Construyendo Aplicaciones Web Modernas' },
      { key: 'ARTICLE_2_DESC', language: 'ES', value: 'Cómo Nodify te ayuda a construir aplicaciones escalables' },
      { key: 'ARTICLE_2_CONTENT', language: 'ES', value: '<h2>Desarrollo Web Moderno</h2><p>Nodify proporciona la base perfecta para construir aplicaciones modernas.</p><h2>Características Clave</h2><ul><li>API RESTful</li><li>Versionado de Contenido</li><li>Gestión de Flujos de Trabajo</li><li>Soporte Multilingüe</li></ul>' },
      
      // Article 3
      { key: 'ARTICLE_3_TITLE', language: 'EN', value: 'API-First Development with Nodify' },
      { key: 'ARTICLE_3_DESC', language: 'EN', value: 'Leverage the power of API-first architecture' },
      { key: 'ARTICLE_3_CONTENT', language: 'EN', value: '<h2>Why API-First?</h2><p>API-first development ensures consistency and better documentation.</p><h2>Nodify\'s API Features</h2><ul><li>RESTful Endpoints</li><li>JWT Authentication</li><li>Filtering & Sorting</li><li>Real-time Updates</li></ul>' },
      { key: 'ARTICLE_3_TITLE', language: 'FR', value: 'Développement API-First avec Nodify' },
      { key: 'ARTICLE_3_DESC', language: 'FR', value: 'Tirez parti de la puissance de l\'architecture API-first' },
      { key: 'ARTICLE_3_CONTENT', language: 'FR', value: '<h2>Pourquoi API-First ?</h2><p>Le développement API-first assure la cohérence et une meilleure documentation.</p><h2>Fonctionnalités API de Nodify</h2><ul><li>Endpoints RESTful</li><li>Authentification JWT</li><li>Filtrage & Tri</li><li>Mises à jour en temps réel</li></ul>' },
      { key: 'ARTICLE_3_TITLE', language: 'ES', value: 'Desarrollo API-First con Nodify' },
      { key: 'ARTICLE_3_DESC', language: 'ES', value: 'Aproveche el poder de la arquitectura API-first' },
      { key: 'ARTICLE_3_CONTENT', language: 'ES', value: '<h2>¿Por qué API-First?</h2><p>El desarrollo API-first asegura consistencia y mejor documentación.</p><h2>Características de la API de Nodify</h2><ul><li>Endpoints RESTful</li><li>Autenticación JWT</li><li>Filtrado & Ordenamiento</li><li>Actualizaciones en tiempo real</li></ul>' }
    ];
    
    for (const article of articlesData) {
      const image = this.images.get(article.imageName);
      const jsonContent = JSON.stringify({
        title: `$translate(${article.titleKey})`,
        description: `$translate(${article.descriptionKey})`,
        content: `$translate(${article.contentKey})`,
        author: article.author,
        date: article.date,
        imageCode: image?.code,
        tags: article.tags
      });
      
      const articleTranslationsForThis = articleTranslations.filter(t => 
        t.key === article.titleKey || t.key === article.descriptionKey || t.key === article.contentKey
      );
      
      const articleNode: ContentNode = {
        parentCode: this.articlesNode!.code,
        type: ContentNodeType.JSON,
        title: article.code,
        code: article.code,
        language: 'EN',
        status: NodeStatus.SNAPSHOT,
        environmentCode: 'production',
        description: `$translate(${article.descriptionKey})`,
        content: jsonContent,
        values: [
          { key: 'AUTHOR', value: article.author },
          { key: 'DATE', value: article.date },
          { key: 'TAGS', value: article.tags.join(',') }
        ],
        tags: article.tags,
        translations: articleTranslationsForThis
      };
      
      const saved = await this.client.saveContentNode(articleNode);
      this.articles.push({ 
        id: saved.code,
        code: article.code,
        titleKey: article.titleKey,
        descriptionKey: article.descriptionKey,
        contentKey: article.contentKey,
        author: article.author,
        date: article.date,
        imageCode: image?.code,
        tags: article.tags,
        translations: articleTranslationsForThis
      });
      console.log(`  ✅ Article created: ${article.code}`);
    }
    console.log('');
  }

  private async createStyleNode(): Promise<void> {
    console.log('🎨 Creating style node (direct child of main site)...');
    
    const css = `
      * { margin: 0; padding: 0; box-sizing: border-box; }
      body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; color: #333; }
      .language-selector { position: fixed; top: 20px; right: 20px; z-index: 1001; background: white; padding: 8px 15px; border-radius: 25px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
      .language-selector select { padding: 5px 10px; border-radius: 15px; border: 1px solid #667eea; cursor: pointer; font-size: 14px; }
      .navbar { background: rgba(255,255,255,0.95); backdrop-filter: blur(10px); box-shadow: 0 2px 20px rgba(0,0,0,0.1); position: sticky; top: 0; z-index: 1000; }
      .nav-container { max-width: 1200px; margin: 0 auto; padding: 1rem 2rem; display: flex; justify-content: space-between; align-items: center; }
      .logo { font-size: 1.5rem; font-weight: bold; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
      .nav-links { display: flex; gap: 2rem; list-style: none; }
      .nav-links a { text-decoration: none; color: #666; transition: color 0.3s; font-weight: 500; cursor: pointer; }
      .nav-links a:hover, .nav-links a.active { color: #667eea; }
      .container { max-width: 1200px; margin: 0 auto; padding: 2rem; }
      .hero { text-align: center; padding: 4rem 2rem; background: rgba(255,255,255,0.95); border-radius: 20px; margin-bottom: 3rem; box-shadow: 0 10px 30px rgba(0,0,0,0.1); }
      .hero h1 { font-size: 3rem; margin-bottom: 1rem; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
      .hero p { font-size: 1.2rem; color: #666; margin-bottom: 2rem; }
      .hero-image { max-width: 100%; height: auto; border-radius: 10px; margin-top: 2rem; }
      .features-section { margin: 4rem 0; }
      .features-section h2 { text-align: center; margin-bottom: 2rem; font-size: 2.5rem; color: white; }
      .features-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 2rem; }
      .feature-card { background: white; border-radius: 15px; padding: 2rem; text-align: center; transition: transform 0.3s; box-shadow: 0 5px 20px rgba(0,0,0,0.1); }
      .feature-card:hover { transform: translateY(-5px); box-shadow: 0 10px 30px rgba(0,0,0,0.2); }
      .feature-card img { width: 100px; height: 100px; border-radius: 50%; margin-bottom: 1rem; object-fit: cover; }
      .feature-card h3 { margin: 1rem 0; color: #667eea; }
      .feature-card p { color: #666; line-height: 1.5; }
      .articles { margin: 4rem 0; }
      .articles h2 { text-align: center; margin-bottom: 2rem; font-size: 2.5rem; color: white; }
      .articles-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(350px, 1fr)); gap: 2rem; }
      .article-card { background: white; border-radius: 15px; overflow: hidden; transition: transform 0.3s; cursor: pointer; box-shadow: 0 5px 20px rgba(0,0,0,0.1); }
      .article-card:hover { transform: translateY(-5px); box-shadow: 0 10px 30px rgba(0,0,0,0.2); }
      .article-card img { width: 100%; height: 200px; object-fit: cover; }
      .article-content { padding: 1.5rem; }
      .article-card h3 { margin-bottom: 0.5rem; color: #333; font-size: 1.25rem; }
      .article-meta { color: #999; font-size: 0.85rem; margin-bottom: 1rem; }
      .article-description { color: #666; margin-bottom: 1rem; line-height: 1.5; }
      .read-more { color: #667eea; font-weight: bold; cursor: pointer; display: inline-block; }
      .read-more:hover { text-decoration: underline; }
      .modal { display: none; position: fixed; z-index: 2000; left: 0; top: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.8); overflow-y: auto; }
      .modal-content { background: white; margin: 50px auto; padding: 2rem; border-radius: 20px; max-width: 800px; position: relative; animation: slideDown 0.3s ease; }
      @keyframes slideDown { from { opacity: 0; transform: translateY(-50px); } to { opacity: 1; transform: translateY(0); } }
      .close { position: absolute; right: 20px; top: 20px; font-size: 2rem; cursor: pointer; color: #999; transition: color 0.3s; }
      .close:hover { color: #333; }
      .modal-body { margin-top: 1rem; }
      .article-full-content { line-height: 1.8; color: #666; }
      .article-full-content h2 { color: #667eea; margin: 1.5rem 0 1rem 0; }
      .article-full-content p { margin-bottom: 1rem; }
      .article-full-content ul, .article-full-content ol { margin-left: 2rem; margin-bottom: 1rem; }
      .article-tags { margin: 1rem 0; padding: 0.75rem; background: #f5f5f5; border-radius: 8px; }
      .tag { display: inline-block; background: #667eea; color: white; padding: 0.25rem 0.75rem; border-radius: 20px; font-size: 0.8rem; margin: 0 0.25rem; }
      .footer { background: rgba(255,255,255,0.95); text-align: center; padding: 2rem; margin-top: 4rem; border-radius: 20px; }
      .footer a { color: #667eea; text-decoration: none; }
      .footer a:hover { text-decoration: underline; }
      @media (max-width: 768px) { .hero h1 { font-size: 2rem; } .nav-container { flex-direction: column; gap: 1rem; } .features-grid { grid-template-columns: 1fr; } .articles-grid { grid-template-columns: 1fr; } .modal-content { margin: 20px; padding: 1rem; } .language-selector { top: 10px; right: 10px; padding: 5px 10px; } }
    `;
    
    this.styleNode = await this.client.saveContentNode({
      parentCode: this.siteNode!.code,
      type: ContentNodeType.STYLE,
      title: 'Landing Page Styles',
      code: this.generateCode('STYLE'),
      language: 'EN',
      status: NodeStatus.SNAPSHOT,
      environmentCode: 'production',
      description: 'CSS styles for the landing page',
      content: css
    });
    console.log(`✅ Style node created: ${this.styleNode.code}\n`);
  }

 private async createScriptNode(): Promise<void> {
  console.log('📜 Creating script node (direct child of main site)...');
  
  const articlesNodeCode = this.articlesNode!.code;
  const featuresNodeCode = this.featuresNode!.code;
  const apiUrl = this.config.apiUrl;
  
  const script = `
    const API_BASE_URL = '${apiUrl}';
    const ARTICLES_NODE_CODE = '${articlesNodeCode}';
    const FEATURES_NODE_CODE = '${featuresNodeCode}';
    
    let currentModal = null;
    let currentLang = 'EN';
    
    // Récupérer la langue depuis l'URL au chargement
    const urlParams = new URLSearchParams(window.location.search);
    const urlLang = urlParams.get('translation');
    if (urlLang && ['EN', 'FR', 'ES'].includes(urlLang)) {
      currentLang = urlLang;
    } else if (localStorage.getItem('language') && ['EN', 'FR', 'ES'].includes(localStorage.getItem('language'))) {
      currentLang = localStorage.getItem('language');
    } else {
      currentLang = 'EN';
    }
    
    function changeLanguage(lang) {
      if (!lang || lang === currentLang) return;
      currentLang = lang;
      localStorage.setItem('language', lang);
      const url = new URL(window.location.href);
      url.searchParams.set('translation', lang);
      window.location.href = url.toString();
    }
    
    function getImageUrl(imageCode) {
      if (!imageCode) return null;
      return API_BASE_URL + '/contents/code/' + imageCode + '/file?status=PUBLISHED';
    }
    
    function escapeHtml(text) {
      if (!text) return '';
      const div = document.createElement('div');
      div.textContent = text;
      return div.innerHTML;
    }
    
    function closeModal() { 
      if (currentModal) { 
        currentModal.style.display = 'none'; 
        currentModal = null; 
        document.body.style.overflow = 'auto'; 
      } 
    }
    
    async function loadFeatures() {
      const grid = document.getElementById('featuresGrid');
      if (!grid) return;
      
      try {
        // 1. Récupérer les métadonnées des features (codes)
        const metadataUrl = API_BASE_URL + '/contents/node/code/' + FEATURES_NODE_CODE + '?status=PUBLISHED';
        const featuresMetadata = await fetch(metadataUrl).then(r => r.json());
        
        if (!Array.isArray(featuresMetadata) || featuresMetadata.length === 0) {
          grid.innerHTML = '<p style="color: white; text-align: center;">No features found.</p>';
          return;
        }
        
        // 2. Récupérer le contenu traduit de chaque feature
        const featuresHtml = [];
        for (const meta of featuresMetadata) {
          const contentUrl = API_BASE_URL + '/contents/code/' + meta.code + '?payloadOnly=true&status=PUBLISHED&translation=' + currentLang;
          const response = await fetch(contentUrl);
          if (response.ok) {
            const featureData = await response.json();
            
            const imageUrl = featureData.imageCode ? getImageUrl(featureData.imageCode) : null;
            const defaultImage = 'https://picsum.photos/id/1/400/300';
            
            featuresHtml.push(\`
              <div class="feature-card">
                <img src="\${imageUrl || defaultImage}" alt="\${escapeHtml(featureData.title)}" loading="lazy" onerror="this.src='\${defaultImage}'">
                <h3>\${escapeHtml(featureData.title)}</h3>
                <p>\${escapeHtml(featureData.description)}</p>
              </div>
            \`);
          }
        }
        
        grid.innerHTML = featuresHtml.join('');
      } catch (error) {
        console.error('Error loading features:', error);
        grid.innerHTML = '<p style="color: white; text-align: center;">Error loading features.</p>';
      }
    }
    
    async function loadArticles() {
      const grid = document.getElementById('articlesGrid');
      if (!grid) return;
      
      try {
        // 1. Récupérer les métadonnées des articles (codes)
        const metadataUrl = API_BASE_URL + '/contents/node/code/' + ARTICLES_NODE_CODE + '?status=PUBLISHED';
        const articlesMetadata = await fetch(metadataUrl).then(r => r.json());
        
        if (!Array.isArray(articlesMetadata) || articlesMetadata.length === 0) {
          grid.innerHTML = '<p style="color: white; text-align: center;">No articles found.</p>';
          return;
        }
        
        // 2. Récupérer le contenu traduit de chaque article
        const articlesHtml = [];
        for (const meta of articlesMetadata) {
          const contentUrl = API_BASE_URL + '/contents/code/' + meta.code + '?payloadOnly=true&status=PUBLISHED&translation=' + currentLang;
          const response = await fetch(contentUrl);
          if (response.ok) {
            const articleData = await response.json();
            
            const imageUrl = articleData.imageCode ? getImageUrl(articleData.imageCode) : null;
            const defaultImage = 'https://picsum.photos/id/1/400/200';
            
            articlesHtml.push(\`
              <div class="article-card" onclick="openArticle('\${meta.code}')">
                <img src="\${imageUrl || defaultImage}" alt="\${escapeHtml(articleData.title)}" loading="lazy" onerror="this.src='\${defaultImage}'">
                <div class="article-content">
                  <h3>\${escapeHtml(articleData.title)}</h3>
                  <div class="article-meta">\${translate('BY')} \${escapeHtml(articleData.author)} | \${new Date(articleData.date).toLocaleDateString()}</div>
                  <p class="article-description">\${escapeHtml(articleData.description.substring(0, 120))}...</p>
                  <span class="read-more">\${translate('READ_MORE')}</span>
                </div>
              </div>
            \`);
          }
        }
        
        grid.innerHTML = articlesHtml.join('');
      } catch (error) {
        console.error('Error loading articles:', error);
        grid.innerHTML = '<p style="color: white; text-align: center;">Error loading articles.</p>';
      }
    }
    
    async function openArticle(articleCode) {
      if (!articleCode) {
        console.error('No article code provided');
        return;
      }
      
      try {
        // Récupérer le contenu traduit de l'article
        const url = API_BASE_URL + '/contents/code/' + articleCode + '?payloadOnly=true&status=PUBLISHED&translation=' + currentLang;
        const response = await fetch(url);
        if (!response.ok) {
          console.error('Failed to fetch article:', url);
          return;
        }
        const articleData = await response.json();
        
        const modal = document.getElementById('articleModal');
        const modalBody = document.getElementById('modalBody');
        
        modalBody.innerHTML = \`
          <h2>\${escapeHtml(articleData.title)}</h2>
          <div class="article-meta">\${translate('BY')} \${escapeHtml(articleData.author)} | \${new Date(articleData.date).toLocaleDateString()}</div>
          <div class="article-tags"><strong>\${translate('TAGS')}</strong> \${articleData.tags.map(t => '<span class="tag">' + escapeHtml(t) + '</span>').join(' ')}</div>
          <div class="article-full-content">\${articleData.content}</div>
        \`;
        modal.style.display = 'block';
        currentModal = modal;
        document.body.style.overflow = 'hidden';
      } catch (error) {
        console.error('Error opening article:', error);
      }
    }
    
    const TRANSLATIONS = {
      EN: ${JSON.stringify(this.mainTranslations.filter(t => t.language === 'EN').reduce((acc, t) => ({ ...acc, [t.key!]: t.value }), {}))},
      FR: ${JSON.stringify(this.mainTranslations.filter(t => t.language === 'FR').reduce((acc, t) => ({ ...acc, [t.key!]: t.value }), {}))},
      ES: ${JSON.stringify(this.mainTranslations.filter(t => t.language === 'ES').reduce((acc, t) => ({ ...acc, [t.key!]: t.value }), {}))}
    };
    
    function translate(key) { 
      return TRANSLATIONS[currentLang][key] || key; 
    }
    
    function updateStaticTranslations() {
      document.querySelectorAll('[data-translate]').forEach(el => {
        const key = el.getAttribute('data-translate');
        if (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA') {
          el.placeholder = translate(key);
        } else {
          el.innerHTML = translate(key);
        }
      });
    }
    
    // Smooth scroll pour les ancres
    document.querySelectorAll('a[href^="#"]').forEach(a => {
      a.addEventListener('click', (e) => {
        e.preventDefault();
        const target = document.querySelector(a.getAttribute('href'));
        if (target) {
          target.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
      });
    });
    
    document.addEventListener('keydown', (e) => { 
      if (e.key === 'Escape') closeModal(); 
    });
    
    window.onclick = (e) => { 
      if (currentModal && e.target === currentModal) closeModal(); 
    };
    
    document.addEventListener('DOMContentLoaded', async () => {
      updateStaticTranslations();
      await loadFeatures();
      await loadArticles();
      
      const select = document.getElementById('langSelect');
      if (select) {
        select.value = currentLang;
        select.onchange = (e) => changeLanguage(e.target.value);
      }
    });
  `;
  
  this.scriptNode = await this.client.saveContentNode({
    parentCode: this.siteNode!.code,
    type: ContentNodeType.SCRIPT,
    title: 'Landing Page Scripts',
    code: this.generateCode('SCRIPT'),
    language: 'EN',
    status: NodeStatus.SNAPSHOT,
    environmentCode: 'production',
    description: 'JavaScript for the landing page',
    content: script
  });
  console.log(`✅ Script node created: ${this.scriptNode.code}\n`);
}

  private async createLandingPage(): Promise<void> {
    console.log('🏠 Creating main landing page HTML node (direct child of main site)...');
    
    const heroImageObj = this.images.get('Hero Image');
    const heroImageUrl = heroImageObj ? `${this.config.apiUrl}/contents/code/${heroImageObj.code}/file?status=PUBLISHED` : 'https://picsum.photos/id/1/1200/600';
    
    const html = `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Nodify - Modern Headless CMS</title>
  <style>$content(${this.styleNode!.code})</style>
</head>
<body>
  <div class="language-selector">
    <select id="langSelect">
      <option value="EN">English</option>
      <option value="FR">Français</option>
      <option value="ES">Español</option>
    </select>
  </div>
  <nav class="navbar">
    <div class="nav-container">
      <div class="logo">✨ Nodify CMS</div>
      <ul class="nav-links">
        <li><a href="#home" data-translate="NAV_HOME">Home</a></li>
        <li><a href="#features" data-translate="NAV_FEATURES">Features</a></li>
        <li><a href="#articles" data-translate="NAV_ARTICLES">Articles</a></li>
        <li><a href="#contact" data-translate="NAV_CONTACT">Contact</a></li>
      </ul>
    </div>
  </nav>
  <main class="container">
    <section id="home" class="hero">
      <h1 data-translate="MAIN_TITLE">Nodify - Modern Headless CMS</h1>
      <p data-translate="MAIN_SUBTITLE">A powerful Headless CMS for modern web applications</p>
      <img src="${heroImageUrl}" class="hero-image" loading="lazy">
    </section>
    
    <section id="features" class="features-section">
      <h2 data-translate="FEATURES_TITLE">✨ Key Features</h2>
      <div id="featuresGrid" class="features-grid"></div>
    </section>
    
    <section id="articles" class="articles">
      <h2 data-translate="ARTICLES_TITLE">📖 Latest Articles</h2>
      <div id="articlesGrid" class="articles-grid"></div>
    </section>
    
    <footer id="contact" class="footer">
      <p>&copy; 2026 Nodify CMS. <span data-translate="FOOTER_TEXT">All rights reserved.</span></p>
      <p><span data-translate="FOOTER_BUILT">Built with ❤️ using</span> <strong>Nodify Headless CMS</strong></p>
    </footer>
  </main>
  <div id="articleModal" class="modal">
    <div class="modal-content">
      <span class="close" onclick="closeModal()">&times;</span>
      <div id="modalBody"></div>
    </div>
  </div>
  <script>$content(${this.scriptNode!.code})</script>
</body>
</html>`;
    
    this.landingPageNode = await this.client.saveContentNode({
      parentCode: this.siteNode!.code,
      type: ContentNodeType.HTML,
      title: 'Nodify Landing Page',
      code: this.generateCode('PAGE'),
      language: 'EN',
      status: NodeStatus.SNAPSHOT,
      environmentCode: 'production',
      description: 'Main landing page for Nodify CMS',
      content: html,
      translations: this.mainTranslations
    });
    console.log(`✅ Landing page node created: ${this.landingPageNode.code}\n`);
  }

  private async publishSite(): Promise<void> {
    console.log('🚀 Publishing main site node (this will publish all children)...');
    
    await this.client.publishNode(this.siteNode!.code!);
    console.log(`✅ Site node published: ${this.siteNode!.code}`);
    console.log(`   All children (style, script, landing page, articles node, features node, pictures node, and their contents) are now published!\n`);
  }

  private displayResults(): void {
    console.log('╔════════════════════════════════════════════════════════════╗');
    console.log('║                    BUILD COMPLETE!                         ║');
    console.log('╚════════════════════════════════════════════════════════════╝\n');
    
    console.log('📊 Structure hiérarchique créée:');
    console.log(`  📁 Site Node (parent): ${this.siteNode!.code}`);
    console.log(`    ├─ 🎨 Style: ${this.styleNode!.code}`);
    console.log(`    ├─ 📜 Script: ${this.scriptNode!.code}`);
    console.log(`    ├─ 📄 Landing Page HTML: ${this.landingPageNode!.code}`);
    console.log(`    ├─ 📁 Articles Node: ${this.articlesNode!.code}`);
    this.articles.forEach(article => {
      console.log(`    │   └─ 📝 ${article.code}`);
    });
    console.log(`    ├─ 📁 Features Node: ${this.featuresNode!.code}`);
    this.features.forEach(feature => {
      console.log(`    │   └─ 📋 ${feature.code}`);
    });
    console.log(`    └─ 📁 Pictures Node: ${this.picturesNode!.code}`);
    this.images.forEach(img => {
      console.log(`        └─ 🖼️ ${img.name}: ${img.code}`);
    });
    
    console.log('\n🌐 Langues disponibles:');
    console.log('   🇬🇧 EN - English (par défaut)');
    console.log('   🇫🇷 FR - Français');
    console.log('   🇪🇸 ES - Español\n');
    
    console.log('🔍 URLs d\'accès:');
    console.log(`  🌐 Landing Page (EN): ${this.config.coreUrl}/v0/content-node/code/${this.landingPageNode!.code}`);
    console.log(`  🌐 Landing Page (FR): ${this.config.coreUrl}/v0/content-node/code/${this.landingPageNode!.code}?translation=FR`);
    console.log(`  🌐 Landing Page (ES): ${this.config.coreUrl}/v0/content-node/code/${this.landingPageNode!.code}?translation=ES`);
    console.log(`  📁 Articles API: ${this.config.apiUrl}/contents/node/code/${this.articlesNode!.code}?status=PUBLISHED`);
    console.log(`  📋 Features API: ${this.config.apiUrl}/contents/node/code/${this.featuresNode!.code}?status=PUBLISHED`);
    console.log(`  🖼️ Images API: ${this.config.apiUrl}/contents/node/code/${this.picturesNode!.code}?status=PUBLISHED\n`);
    
    console.log('💡 Comment changer de langue:');
    console.log('  1. Utilisez le sélecteur de langue en haut à droite');
    console.log('  2. La page se recharge automatiquement avec ?translation=XX');
    console.log('  3. Les articles et features sont automatiquement traduits via $translate()');
    console.log('  4. La langue par défaut est EN\n');
  }
}

async function main() {
  const builder = new LandingPageBuilder();
  await builder.build().catch(error => {
    console.error('Fatal error:', error);
    process.exit(1);
  });
}

process.on('SIGINT', () => {
  console.log('\n\n👋 Gracefully shutting down...');
  process.exit(0);
});

main();