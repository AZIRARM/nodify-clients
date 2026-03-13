
import {
  NodifyClientConfig,
  Plugin,
  PluginFile,
  UserPost,
  UserPassword,
  UserRole,
  UserParameters,
  Notification,
  ContentNode,
  Node,
  Language,
  Feedback,
  Environment,
  Data,
  ContentNodePayload,
  ContentDisplay,
  ContentClick,
  AccessRole,
  UserLogin,
  AuthResponse,
  TreeNode,
  LockInfo,
  FeedbackCharts,
  ContentDisplayCharts,
  ContentClickCharts,
  PaginationParams,
  NodeStatus,
  NodifyError
} from './models/nodify-models';

export class NodifyClient {
  private config: NodifyClientConfig;
  private authToken: string | null = null;
  private refreshPromise: Promise<string | null> | null = null;
  
  constructor(config: NodifyClientConfig) {
    this.config = config;
    this.authToken = config.authToken || null;
  }

  static builder(): NodifyClientBuilder {
    return new NodifyClientBuilder();
  }

  // Méthodes d'authentification
  async login(email: string, password: string): Promise<AuthResponse> {
    const response = await this.request<AuthResponse>('/authentication/login', {
      method: 'POST',
      body: JSON.stringify({ email, password })
    }, false); // Pas besoin de token pour le login

    if (response.token) {
      this.authToken = response.token;
    }

    return response;
  }

  logout(): void {
    this.authToken = null;
  }

  setAuthToken(token: string): void {
    this.authToken = token;
  }

  getAuthToken(): string | null {
    return this.authToken;
  }

  private getHeaders(includeAuth: boolean = true): HeadersInit {
    const headers: Record<string, string> = {
      ...this.config.defaultHeaders
    };

    if (includeAuth && this.authToken) {
      headers['Authorization'] = `Bearer ${this.authToken}`;
    }

    return headers;
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {},
    retryOnAuthError: boolean = true
  ): Promise<T> {
    const url = `${this.config.baseUrl}${endpoint}`;
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), this.config.timeout);

    try {
      const response = await fetch(url, {
        ...options,
        headers: {
          ...this.getHeaders(),
          ...options.headers,
        },
        signal: controller.signal
      });

      clearTimeout(timeoutId);

      // Gestion des erreurs d'authentification
      if (response.status === 401 && retryOnAuthError && this.config.onAuthError) {
        if (!this.refreshPromise) {
          this.refreshPromise = this.config.onAuthError();
        }

        const newToken = await this.refreshPromise;
        this.refreshPromise = null;

        if (newToken) {
          this.authToken = newToken;
          // Retry la requête avec le nouveau token
          return this.request<T>(endpoint, options, false);
        }
      }

      if (!response.ok) {
        const errorText = await response.text();
        throw new NodifyError(
          `HTTP ${response.status}: ${errorText || response.statusText}`,
          response.status
        );
      }

      if (response.status === 204) {
        return {} as T;
      }

      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        return await response.json();
      }

      if (contentType && contentType.includes('text/plain')) {
        return await response.text() as unknown as T;
      }

      // Pour les fichiers binaires
      const arrayBuffer = await response.arrayBuffer();
      return new Uint8Array(arrayBuffer) as unknown as T;
    } catch (error) {
      clearTimeout(timeoutId);
      
      if (error instanceof NodifyError) {
        throw error;
      }
      
      if (error instanceof Error && error.name === 'AbortError') {
        throw new NodifyError(`Request timeout after ${this.config.timeout}ms`);
      }
      
      throw new NodifyError(error instanceof Error ? error.message : 'Unknown error');
    }
  }

  // ============ Health ============
  async health(): Promise<string> {
    return this.request<string>('/health', {
      method: 'GET'
    });
  }

  // ============ Users ============
  async findAllUsers(): Promise<UserPost[]> {
    return this.request<UserPost[]>('/v0/users/', {
      method: 'GET'
    });
  }

  async saveUser(user: UserPost): Promise<UserPost> {
    return this.request<UserPost>('/v0/users/', {
      method: 'POST',
      body: JSON.stringify(user)
    });
  }

  async changePassword(passwordData: UserPassword): Promise<boolean> {
    return this.request<boolean>('/v0/users/password', {
      method: 'POST',
      body: JSON.stringify(passwordData)
    });
  }

  async findUserById(id: string): Promise<UserPost> {
    return this.request<UserPost>(`/v0/users/id/${id}`, {
      method: 'GET'
    });
  }

  async findUserByEmail(email: string): Promise<UserPost> {
    return this.request<UserPost>(`/v0/users/email/${email}`, {
      method: 'GET'
    });
  }

  async deleteUser(id: string): Promise<boolean> {
    return this.request<boolean>(`/v0/users/id/${id}`, {
      method: 'DELETE'
    });
  }

  // ============ User Roles ============
  async findAllUserRoles(): Promise<UserRole[]> {
    return this.request<UserRole[]>('/v0/users-roles/', {
      method: 'GET'
    });
  }

  async saveUserRole(userRole: UserRole): Promise<UserRole> {
    return this.request<UserRole>('/v0/users-roles/', {
      method: 'POST',
      body: JSON.stringify(userRole)
    });
  }

  async findUserRoleById(id: string): Promise<UserRole> {
    return this.request<UserRole>(`/v0/users-roles/id/${id}`, {
      method: 'GET'
    });
  }

  async deleteUserRole(id: string): Promise<boolean> {
    return this.request<boolean>(`/v0/users-roles/id/${id}`, {
      method: 'DELETE'
    });
  }

  // ============ User Parameters ============
  async findAllUserParameters(): Promise<UserParameters[]> {
    return this.request<UserParameters[]>('/v0/user-parameters/', {
      method: 'GET'
    });
  }

  async saveUserParameters(params: UserParameters): Promise<UserParameters> {
    return this.request<UserParameters>('/v0/user-parameters/', {
      method: 'POST',
      body: JSON.stringify(params)
    });
  }

  async findUserParametersByUserId(userId: string): Promise<UserParameters> {
    return this.request<UserParameters>(`/v0/user-parameters/user/${userId}`, {
      method: 'GET'
    });
  }

  async findUserParametersById(id: string): Promise<UserParameters> {
    return this.request<UserParameters>(`/v0/user-parameters/id/${id}`, {
      method: 'GET'
    });
  }

  async deleteUserParameters(id: string): Promise<boolean> {
    return this.request<boolean>(`/v0/user-parameters/id/${id}`, {
      method: 'DELETE'
    });
  }

  // ============ Plugins ============
  async findNotDeletedPlugins(): Promise<Plugin[]> {
    return this.request<Plugin[]>('/v0/plugins/', {
      method: 'GET'
    });
  }

  async savePlugin(plugin: Plugin): Promise<Plugin> {
    return this.request<Plugin>('/v0/plugins/', {
      method: 'POST',
      body: JSON.stringify(plugin)
    });
  }

  async importContentNodePlugin(plugin: Plugin): Promise<Plugin> {
    return this.request<Plugin>('/v0/plugins/import', {
      method: 'POST',
      body: JSON.stringify(plugin)
    });
  }

  async enablePlugin(id: string): Promise<Plugin> {
    return this.request<Plugin>(`/v0/plugins/id/${id}/enable`, {
      method: 'PUT'
    });
  }

  async disablePlugin(id: string): Promise<Plugin> {
    return this.request<Plugin>(`/v0/plugins/id/${id}/disable`, {
      method: 'PUT'
    });
  }

  async activatePlugin(id: string): Promise<Plugin> {
    return this.request<Plugin>(`/v0/plugins/id/${id}/activate`, {
      method: 'PUT'
    });
  }

  async findPluginById(id: string): Promise<Plugin> {
    return this.request<Plugin>(`/v0/plugins/id/${id}`, {
      method: 'GET'
    });
  }

  async exportPlugin(id: string): Promise<Uint8Array> {
    return this.request<Uint8Array>(`/v0/plugins/id/${id}/export`, {
      method: 'GET'
    });
  }

  async findDeletedPlugins(): Promise<Plugin[]> {
    return this.request<Plugin[]>('/v0/plugins/deleteds', {
      method: 'GET'
    });
  }

  async deletePlugin(id: string): Promise<boolean> {
    return this.request<boolean>(`/v0/plugins/id/${id}`, {
      method: 'DELETE'
    });
  }

  async deletePluginDefinitively(id: string): Promise<boolean> {
    return this.request<boolean>(`/v0/plugins/id/${id}/deleteDefinitively`, {
      method: 'DELETE'
    });
  }

  // ============ Plugin Files ============
  async findAllPluginFiles(enabled?: boolean): Promise<PluginFile[]> {
    const url = enabled !== undefined 
      ? `/v0/plugin-files/?enabled=${enabled}`
      : '/v0/plugin-files/';
    
    return this.request<PluginFile[]>(url, {
      method: 'GET'
    });
  }

  async savePluginFile(pluginFile: PluginFile): Promise<PluginFile> {
    return this.request<PluginFile>('/v0/plugin-files/', {
      method: 'POST',
      body: JSON.stringify(pluginFile)
    });
  }

  async findPluginFilesByPluginId(id: string): Promise<PluginFile[]> {
    return this.request<PluginFile[]>(`/v0/plugin-files/plugin/${id}`, {
      method: 'GET'
    });
  }

  async findPluginFilesByPluginName(name: string): Promise<PluginFile[]> {
    return this.request<PluginFile[]>(`/v0/plugin-files/plugin/name/${name}`, {
      method: 'GET'
    });
  }

  async findPluginFileById(id: string): Promise<PluginFile> {
    return this.request<PluginFile>(`/v0/plugin-files/id/${id}`, {
      method: 'GET'
    });
  }

  async deletePluginFile(id: string): Promise<boolean> {
    return this.request<boolean>(`/v0/plugin-files/id/${id}`, {
      method: 'DELETE'
    });
  }

  // ============ Notifications ============
  async markAllNotificationsAsRead(): Promise<Notification[]> {
    return this.request<Notification[]>('/v0/notifications/markAllAsRead', {
      method: 'POST'
    });
  }

  async markNotificationAsRead(notificationId: string): Promise<Notification> {
    return this.request<Notification>(`/v0/notifications/id/${notificationId}/markread`, {
      method: 'POST'
    });
  }

  // ============ Nodes ============
  async findAllNodes(): Promise<Node[]> {
    return this.request<Node[]>('/v0/nodes/', {
      method: 'GET'
    });
  }

  async saveNode(node: Node): Promise<Node> {
    return this.request<Node>('/v0/nodes/', {
      method: 'POST',
      body: JSON.stringify(node)
    });
  }

  async propagateMaxHistoryToKeep(nodeCodeParent: string): Promise<boolean> {
    return this.request<boolean>(`/v0/nodes/propagateMaxHistoryToKeep/${nodeCodeParent}`, {
      method: 'POST'
    });
  }

  async importNode(node: Node): Promise<Node> {
    return this.request<Node>('/v0/nodes/import', {
      method: 'POST',
      body: JSON.stringify(node)
    });
  }

  async importNodes(nodes: Node[], nodeParentCode?: string, fromFile: boolean = true): Promise<Node[]> {
    const params = new URLSearchParams();
    if (nodeParentCode) params.set('nodeParentCode', nodeParentCode);
    params.set('fromFile', fromFile.toString());
    
    return this.request<Node[]>(`/v0/nodes/importAll?${params.toString()}`, {
      method: 'POST',
      body: JSON.stringify(nodes)
    });
  }

  async revertNodeVersion(code: string, version: string): Promise<Node> {
    return this.request<Node>(`/v0/nodes/code/${code}/version/${version}/revert`, {
      method: 'POST'
    });
  }

  async deployNodeVersion(code: string, version: string, environment?: string): Promise<boolean> {
    const params = new URLSearchParams();
    if (environment) params.set('environment', environment);
    
    const url = `/v0/nodes/code/${code}/version/${version}/deploy${params.toString() ? `?${params.toString()}` : ''}`;
    return this.request<boolean>(url, {
      method: 'POST'
    });
  }

  async publishNode(code: string): Promise<Node> {
    return this.request<Node>(`/v0/nodes/code/${code}/publish`, {
      method: 'POST'
    });
  }

  async activateNode(code: string): Promise<boolean> {
    return this.request<boolean>(`/v0/nodes/code/${code}/activate`, {
      method: 'POST'
    });
  }

  async findAllNodesByStatus(status: string): Promise<Node[]> {
    return this.request<Node[]>(`/v0/nodes/status/${status}`, {
      method: 'GET'
    });
  }

  async findPublishedNodes(): Promise<Node[]> {
    return this.request<Node[]>('/v0/nodes/published', {
      method: 'GET'
    });
  }

  async findParentNodesByStatus(status: string): Promise<Node[]> {
    return this.request<Node[]>(`/v0/nodes/parent/status/${status}`, {
      method: 'GET'
    });
  }

  async findNodesByParentCode(code: string): Promise<Node[]> {
    return this.request<Node[]>(`/v0/nodes/parent/code/${code}`, {
      method: 'GET'
    });
  }

  async findChildrenByCodeAndStatus(code: string, status: string): Promise<Node[]> {
    return this.request<Node[]>(`/v0/nodes/parent/code/${code}/status/${status}`, {
      method: 'GET'
    });
  }

  async findAllDescendants(code: string): Promise<Node[]> {
    return this.request<Node[]>(`/v0/nodes/parent/code/${code}/descendants`, {
      method: 'GET'
    });
  }

  async findParentOrigin(): Promise<Node[]> {
    return this.request<Node[]>('/v0/nodes/origin', {
      method: 'GET'
    });
  }

  async getDeletedNodes(parent?: string): Promise<Node[]> {
    const url = parent 
      ? `/v0/nodes/deleted?parent=${parent}`
      : '/v0/nodes/deleted';
    
    return this.request<Node[]>(url, {
      method: 'GET'
    });
  }

  async findNodesByCode(code: string): Promise<Node[]> {
    return this.request<Node[]>(`/v0/nodes/code/${code}`, {
      method: 'GET'
    });
  }

  async findNodeByCodeAndStatus(code: string, status: string): Promise<Node> {
    return this.request<Node>(`/v0/nodes/code/${code}/status/${status}`, {
      method: 'GET'
    });
  }

  async generateTreeView(code: string): Promise<TreeNode> {
    return this.request<TreeNode>(`/v0/nodes/code/${code}/tree-view`, {
      method: 'GET'
    });
  }

  async checkSlugExists(code: string, slug: string): Promise<boolean> {
    return this.request<boolean>(`/v0/nodes/code/${code}/slug/${slug}/exists`, {
      method: 'GET'
    });
  }

  async hasContents(code: string): Promise<boolean> {
    return this.request<boolean>(`/v0/nodes/code/${code}/haveContents`, {
      method: 'GET'
    });
  }

  async hasChildren(code: string): Promise<boolean> {
    return this.request<boolean>(`/v0/nodes/code/${code}/haveChilds`, {
      method: 'GET'
    });
  }

  async exportAllNodes(code: string, environment?: string): Promise<Uint8Array> {
    const url = environment 
      ? `/v0/nodes/code/${code}/export?environment=${environment}`
      : `/v0/nodes/code/${code}/export`;
    
    return this.request<Uint8Array>(url, {
      method: 'GET'
    });
  }

  async deployNode(code: string, environment?: string): Promise<Node[]> {
    const url = environment 
      ? `/v0/nodes/code/${code}/deploy?environment=${environment}`
      : `/v0/nodes/code/${code}/deploy`;
    
    return this.request<Node[]>(url, {
      method: 'GET'
    });
  }

  async deleteNode(code: string): Promise<boolean> {
    return this.request<boolean>(`/v0/nodes/code/${code}`, {
      method: 'DELETE'
    });
  }

  async deleteNodeDefinitively(code: string): Promise<boolean> {
    return this.request<boolean>(`/v0/nodes/code/${code}/deleteDefinitively`, {
      method: 'DELETE'
    });
  }

  async deleteNodeVersionDefinitively(code: string, version: string): Promise<boolean> {
    return this.request<boolean>(`/v0/nodes/code/${code}/version/${version}/deleteDefinitively`, {
      method: 'DELETE'
    });
  }

  // ============ Locks ============
  async acquireLock(code: string): Promise<boolean> {
    return this.request<boolean>(`/v0/locks/acquire/${code}`, {
      method: 'POST'
    });
  }

  async refreshLock(code: string): Promise<boolean> {
    return this.request<boolean>(`/v0/locks/refresh/${code}`, {
      method: 'POST'
    });
  }

  async releaseLock(code: string): Promise<boolean> {
    return this.request<boolean>(`/v0/locks/release/${code}`, {
      method: 'POST'
    });
  }

  async adminReleaseLock(code: string): Promise<boolean> {
    return this.request<boolean>(`/v0/locks/admin/release/${code}`, {
      method: 'POST'
    });
  }

  async getLockOwner(code: string): Promise<LockInfo> {
    return this.request<LockInfo>(`/v0/locks/owner/${code}`, {
      method: 'GET'
    });
  }

  async getAllLocks(): Promise<LockInfo[]> {
    return this.request<LockInfo[]>('/v0/locks/all', {
      method: 'GET'
    });
  }

  // ============ Languages ============
  async findAllLanguages(): Promise<Language[]> {
    return this.request<Language[]>('/v0/languages/', {
      method: 'GET'
    });
  }

  async saveLanguage(language: Language): Promise<Language> {
    return this.request<Language>('/v0/languages/', {
      method: 'POST',
      body: JSON.stringify(language)
    });
  }

  async findLanguageById(id: string): Promise<Language> {
    return this.request<Language>(`/v0/languages/id/${id}`, {
      method: 'GET'
    });
  }

  async deleteLanguage(id: string): Promise<boolean> {
    return this.request<boolean>(`/v0/languages/id/${id}`, {
      method: 'DELETE'
    });
  }

  // ============ Feedback ============
  async findAllFeedback(): Promise<Feedback[]> {
    return this.request<Feedback[]>('/v0/feedbacks/', {
      method: 'GET'
    });
  }

  async saveFeedback(feedback: Feedback): Promise<Feedback> {
    return this.request<Feedback>('/v0/feedbacks/', {
      method: 'POST',
      body: JSON.stringify(feedback)
    });
  }

  async findFeedbackByVerified(verified: boolean): Promise<Feedback[]> {
    return this.request<Feedback[]>(`/v0/feedbacks/verified/${verified}`, {
      method: 'GET'
    });
  }

  async findFeedbackByUserId(userId: string): Promise<Feedback[]> {
    return this.request<Feedback[]>(`/v0/feedbacks/userId/${userId}`, {
      method: 'GET'
    });
  }

  async findFeedbackById(id: string): Promise<Feedback> {
    return this.request<Feedback>(`/v0/feedbacks/id/${id}`, {
      method: 'GET'
    });
  }

  async findFeedbackByEvaluation(evaluation: number): Promise<Feedback[]> {
    return this.request<Feedback[]>(`/v0/feedbacks/evaluation/${evaluation}`, {
      method: 'GET'
    });
  }

  async findFeedbackByContentCode(code: string): Promise<Feedback[]> {
    return this.request<Feedback[]>(`/v0/feedbacks/contentCode/${code}`, {
      method: 'GET'
    });
  }

  async getContentCharts(): Promise<FeedbackCharts[]> {
    return this.request<FeedbackCharts[]>('/v0/feedbacks/charts', {
      method: 'GET'
    });
  }

  async getContentChartsByNodeAndUser(code: string): Promise<FeedbackCharts[]> {
    return this.request<FeedbackCharts[]>(`/v0/feedbacks/charts/node/${code}`, {
      method: 'GET'
    });
  }

  async getChartsByContentCode(code: string): Promise<FeedbackCharts[]> {
    return this.request<FeedbackCharts[]>(`/v0/feedbacks/charts/content/${code}`, {
      method: 'GET'
    });
  }

  async deleteFeedback(id: string): Promise<boolean> {
    return this.request<boolean>(`/v0/feedbacks/id/${id}`, {
      method: 'DELETE'
    });
  }

  // ============ Environments ============
  async findAllEnvironments(): Promise<Environment[]> {
    return this.request<Environment[]>('/v0/environments/', {
      method: 'GET'
    });
  }

  async saveEnvironment(environment: Environment): Promise<Environment> {
    return this.request<Environment>('/v0/environments/', {
      method: 'POST',
      body: JSON.stringify(environment)
    });
  }

  async saveAllEnvironments(environments: Environment[]): Promise<Environment[]> {
    return this.request<Environment[]>('/v0/environments/saveAll', {
      method: 'POST',
      body: JSON.stringify(environments)
    });
  }

  async findEnvironmentById(id: string): Promise<Environment> {
    return this.request<Environment>(`/v0/environments/id/${id}`, {
      method: 'GET'
    });
  }

  async deleteEnvironmentByCode(code: string): Promise<boolean> {
    return this.request<boolean>(`/v0/environments/code/${code}`, {
      method: 'DELETE'
    });
  }

  // ============ Data ============
  async saveData(data: Data): Promise<Data> {
    return this.request<Data>('/v0/datas/', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  }

  async findDataByKey(key: string): Promise<Data> {
    return this.request<Data>(`/v0/datas/key/${key}`, {
      method: 'GET'
    });
  }

  async findDataByContentCode(code: string, params?: PaginationParams): Promise<Data[]> {
    const urlParams = new URLSearchParams();
    if (params?.currentPage !== undefined) urlParams.set('currentPage', params.currentPage.toString());
    if (params?.limit !== undefined) urlParams.set('limit', params.limit.toString());
    
    const queryString = urlParams.toString();
    const endpoint = `/v0/datas/contentCode/${code}${queryString ? `?${queryString}` : ''}`;
    
    return this.request<Data[]>(endpoint, {
      method: 'GET'
    });
  }

  async countDataByContentCode(code: string): Promise<number> {
    return this.request<number>(`/v0/datas/contentCode/${code}/count`, {
      method: 'GET'
    });
  }

  async deleteAllDataByContentCode(code: string): Promise<boolean> {
    return this.request<boolean>(`/v0/datas/contentCode/${code}`, {
      method: 'DELETE'
    });
  }

  async deleteDataById(uuid: string): Promise<boolean> {
    return this.request<boolean>(`/v0/datas/id/${uuid}`, {
      method: 'DELETE'
    });
  }

  // ============ Content Nodes ============
  async findAllContentNodes(): Promise<ContentNode[]> {
    return this.request<ContentNode[]>('/v0/content-node/', {
      method: 'GET'
    });
  }

  async saveContentNode(contentNode: ContentNode): Promise<ContentNode> {
    return this.request<ContentNode>('/v0/content-node/', {
      method: 'POST',
      body: JSON.stringify(contentNode)
    });
  }

  async importContentNode(contentNode: ContentNode, nodeParentCode?: string, fromFile: boolean = true): Promise<ContentNode> {
    const params = new URLSearchParams();
    if (nodeParentCode) params.set('nodeParentCode', nodeParentCode);
    params.set('fromFile', fromFile.toString());
    
    return this.request<ContentNode>(`/v0/content-node/import?${params.toString()}`, {
      method: 'POST',
      body: JSON.stringify(contentNode)
    });
  }

  async revertContentNodeVersion(code: string, version: string): Promise<ContentNode> {
    return this.request<ContentNode>(`/v0/content-node/code/${code}/version/${version}/revert`, {
      method: 'POST'
    });
  }

  async deployContentNodeVersion(code: string, version: string, environment?: string): Promise<boolean> {
    const params = new URLSearchParams();
    if (environment) params.set('environment', environment);
    
    const url = `/v0/content-node/code/${code}/version/${version}/deploy${params.toString() ? `?${params.toString()}` : ''}`;
    return this.request<boolean>(url, {
      method: 'POST'
    });
  }

  async fillContent(code: string, status: NodeStatus, payload: ContentNodePayload): Promise<ContentNode> {
    return this.request<ContentNode>(`/v0/content-node/code/${code}/status/${status}/fill`, {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  }

  async publishContentNode(code: string, publish: boolean): Promise<ContentNode> {
    return this.request<ContentNode>(`/v0/content-node/code/${code}/publish/${publish}`, {
      method: 'POST'
    });
  }

    async activateContentNode(code: string): Promise<boolean> {
    return this.request<boolean>(`/v0/content-node/code/${code}/activate`, {
      method: 'POST'
    });
  }

  async findAllContentNodesByStatus(status: string): Promise<ContentNode[]> {
    return this.request<ContentNode[]>(`/v0/content-node/status/${status}`, {
      method: 'GET'
    });
  }

  async findAllContentNodesByNodeCode(code: string): Promise<ContentNode[]> {
    return this.request<ContentNode[]>(`/v0/content-node/node/code/${code}`, {
      method: 'GET'
    });
  }

  async findContentNodesByNodeCodeAndStatus(code: string, status: string): Promise<ContentNode[]> {
    return this.request<ContentNode[]>(`/v0/content-node/node/code/${code}/status/${status}`, {
      method: 'GET'
    });
  }

  async getDeletedContentNodes(parent?: string): Promise<ContentNode[]> {
    const url = parent 
      ? `/v0/content-node/deleted?parent=${parent}`
      : '/v0/content-node/deleted';
    
    return this.request<ContentNode[]>(url, {
      method: 'GET'
    });
  }

  async findAllContentNodesByCode(code: string): Promise<ContentNode[]> {
    return this.request<ContentNode[]>(`/v0/content-node/code/${code}`, {
      method: 'GET'
    });
  }

  async findContentNodeByCodeAndStatus(code: string, status: string): Promise<ContentNode> {
    return this.request<ContentNode>(`/v0/content-node/code/${code}/status/${status}`, {
      method: 'GET'
    });
  }

  async checkContentNodeSlugExists(code: string, slug: string): Promise<boolean> {
    return this.request<boolean>(`/v0/content-node/code/${code}/slug/${slug}/exists`, {
      method: 'GET'
    });
  }

  async exportContentNode(code: string, environment?: string): Promise<Uint8Array> {
    const url = environment 
      ? `/v0/content-node/code/${code}/export?environment=${environment}`
      : `/v0/content-node/code/${code}/export`;
    
    return this.request<Uint8Array>(url, {
      method: 'GET'
    });
  }

  async deployContentNode(code: string, environment?: string): Promise<boolean> {
    const url = environment 
      ? `/v0/content-node/code/${code}/deploy?environment=${environment}`
      : `/v0/content-node/code/${code}/deploy`;
    
    return this.request<boolean>(url, {
      method: 'GET'
    });
  }

  async deleteContentNode(code: string): Promise<boolean> {
    return this.request<boolean>(`/v0/content-node/code/${code}`, {
      method: 'DELETE'
    });
  }

  async deleteContentNodeDefinitively(code: string): Promise<boolean> {
    return this.request<boolean>(`/v0/content-node/code/${code}/deleteDefinitively`, {
      method: 'DELETE'
    });
  }

  async deleteContentNodeVersionDefinitively(code: string, version: string): Promise<boolean> {
    return this.request<boolean>(`/v0/content-node/code/${code}/version/${version}/deleteDefinitively`, {
      method: 'DELETE'
    });
  }

  // ============ Content Displays ============
  async findAllContentDisplays(): Promise<ContentDisplay[]> {
    return this.request<ContentDisplay[]>('/v0/content-displays/', {
      method: 'GET'
    });
  }

  async saveContentDisplay(contentDisplay: ContentDisplay): Promise<ContentDisplay> {
    return this.request<ContentDisplay>('/v0/content-displays/', {
      method: 'POST',
      body: JSON.stringify(contentDisplay)
    });
  }

  async findContentDisplayById(id: string): Promise<ContentDisplay> {
    return this.request<ContentDisplay>(`/v0/content-displays/id/${id}`, {
      method: 'GET'
    });
  }

  async findContentDisplayByContentCode(code: string): Promise<ContentDisplay> {
    return this.request<ContentDisplay>(`/v0/content-displays/contentCode/${code}`, {
      method: 'GET'
    });
  }

  async getContentDisplayCharts(): Promise<ContentDisplayCharts[]> {
    return this.request<ContentDisplayCharts[]>('/v0/content-displays/charts', {
      method: 'GET'
    });
  }

  async getChartsByNodeCode(code: string): Promise<ContentDisplayCharts[]> {
    return this.request<ContentDisplayCharts[]>(`/v0/content-displays/charts/node/${code}`, {
      method: 'GET'
    });
  }

  async getChartsByContentCode(code: string): Promise<ContentDisplayCharts[]> {
    return this.request<ContentDisplayCharts[]>(`/v0/content-displays/charts/content/${code}`, {
      method: 'GET'
    });
  }

  async deleteContentDisplay(id: string): Promise<boolean> {
    return this.request<boolean>(`/v0/content-displays/id/${id}`, {
      method: 'DELETE'
    });
  }

  // ============ Content Clicks ============
  async findAllContentClicks(): Promise<ContentClick[]> {
    return this.request<ContentClick[]>('/v0/content-clicks/', {
      method: 'GET'
    });
  }

  async saveContentClick(contentClick: ContentClick): Promise<ContentClick> {
    return this.request<ContentClick>('/v0/content-clicks/', {
      method: 'POST',
      body: JSON.stringify(contentClick)
    });
  }

  async findContentClickById(id: string): Promise<ContentClick> {
    return this.request<ContentClick>(`/v0/content-clicks/id/${id}`, {
      method: 'GET'
    });
  }

  async findContentClickByContentCode(code: string): Promise<ContentClick[]> {
    return this.request<ContentClick[]>(`/v0/content-clicks/contentCode/${code}`, {
      method: 'GET'
    });
  }

  async getContentClickCharts(): Promise<ContentClickCharts[]> {
    return this.request<ContentClickCharts[]>('/v0/content-clicks/charts', {
      method: 'GET'
    });
  }

  async getChartsByNodeCodeForClicks(code: string): Promise<ContentClickCharts[]> {
    return this.request<ContentClickCharts[]>(`/v0/content-clicks/charts/node/${code}`, {
      method: 'GET'
    });
  }

  async getChartsByContentCodeForClicks(code: string): Promise<ContentClickCharts[]> {
    return this.request<ContentClickCharts[]>(`/v0/content-clicks/charts/content/${code}`, {
      method: 'GET'
    });
  }

  async deleteContentClick(id: string): Promise<boolean> {
    return this.request<boolean>(`/v0/content-clicks/id/${id}`, {
      method: 'DELETE'
    });
  }

  // ============ Access Roles ============
  async findAllAccessRoles(): Promise<AccessRole[]> {
    return this.request<AccessRole[]>('/v0/access-roles/', {
      method: 'GET'
    });
  }

  async saveAccessRole(accessRole: AccessRole): Promise<AccessRole> {
    return this.request<AccessRole>('/v0/access-roles/', {
      method: 'POST',
      body: JSON.stringify(accessRole)
    });
  }

  async findAccessRoleById(id: string): Promise<AccessRole> {
    return this.request<AccessRole>(`/v0/access-roles/id/${id}`, {
      method: 'GET'
    });
  }

  async deleteAccessRole(id: string): Promise<boolean> {
    return this.request<boolean>(`/v0/access-roles/id/${id}`, {
      method: 'DELETE'
    });
  }

  // ============ Charts ============
  async getCharts(): Promise<TreeNode> {
    return this.request<TreeNode>('/v0/charts/', {
      method: 'GET'
    });
  }

  // ============ Slugs ============
  async checkSlugExists(slug: string): Promise<string[]> {
    return this.request<string[]>(`/v0/slugs/exists/${slug}`, {
      method: 'GET'
    });
  }
}