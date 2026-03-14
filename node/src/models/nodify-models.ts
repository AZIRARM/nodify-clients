export enum ContentNodeType {
  FILE = 'FILE',
  PICTURE = 'PICTURE',
  SCRIPT = 'SCRIPT',
  STYLE = 'STYLE',
  HTML = 'HTML',
  JSON = 'JSON',
  URLS = 'URLS',
  DATA = 'DATA',
  XML = 'XML'
}

export enum ContentUrlType {
  API = 'API',
  PAGE = 'PAGE',
  SCRIPT = 'SCRIPT',
  STYLE = 'STYLE',
  MEDIA = 'MEDIA'
}

export enum RuleType {
  BOOL = 'BOOL',
  FLOAT = 'FLOAT',
  NUM = 'NUM',
  DATE = 'DATE',
  STRING = 'STRING'
}

export enum RuleOperator {
  EQ = 'EQ',
  SUP = 'SUP',
  LOW = 'LOW',
  SUP_EQ = 'SUP_EQ',
  LOW_EQ = 'LOW_EQ',
  DIF = 'DIF'
}

export enum NodeStatus {
  SNAPSHOT = 'SNAPSHOT',
  PUBLISHED = 'PUBLISHED',
  ARCHIVE = 'ARCHIVE',
  DELETED = 'DELETED',
  NEW = 'NEW'
}

// Interfaces principales
export interface Plugin {
  id?: string;
  enabled?: boolean;
  editable?: boolean;
  description?: string;
  name?: string;
  code?: string;
  entrypoint?: string;
  creationDate?: number;
  modificationDate?: number;
  modifiedBy?: string;
  deleted?: boolean;
  resources?: PluginFile[];
}

export interface PluginFile {
  id?: string;
  pluginId?: string;
  fileName?: string;
  description?: string;
  data?: string;
}

export interface UserPost {
  id?: string;
  firstname?: string;
  lastname?: string;
  email?: string;
  password?: string;
  roles?: string[];
  projects?: string[];
}

export interface UserPassword {
  userId?: string;
  password?: string;
  newPassword?: string;
}

export interface UserRole {
  id?: string;
  name?: string;
  description?: string;
  code?: string;
}

export interface UserParameters {
  id?: string;
  userId?: string;
  acceptNotifications?: boolean;
  defaultLanguage?: string;
  theme?: string;
  ai?: boolean;
}

export interface Notification {
  id?: string;
  type?: string;
  typeCode?: string;
  typeVersion?: string;
  code?: string;
  date?: number;
  description?: string;
  user?: string;
  modifiedBy?: string;
  read?: boolean;
}

export interface ContentFile {
  name?: string;
  type?: string;
  data?: string;
  size?: number;
}

export interface ContentUrl {
  id?: string;
  url?: string;
  description?: string;
  type?: ContentUrlType;
}

export interface Value {
  id?: string;
  key?: string;
  value?: string;
}

export interface Rule {
  type?: RuleType;
  name?: string;
  value?: string;
  editable?: boolean;
  erasable?: boolean;
  operator?: RuleOperator;
  behavior?: boolean;
  enable?: boolean;
  description?: string;
}

export interface Translation {
  language?: string;
  key?: string;
  value?: string;
}

export interface ContentNode {
  id?: string;
  parentCode?: string;
  parentCodeOrigin?: string;
  code?: string;
  slug?: string;
  environmentCode?: string;
  language?: string;
  type?: ContentNodeType;
  title?: string;
  description?: string;
  redirectUrl?: string;
  iconUrl?: string;
  pictureUrl?: string;
  content?: string;
  urls?: ContentUrl[];
  file?: ContentFile;
  tags?: string[];
  values?: Value[];
  roles?: string[];
  rules?: Rule[];
  creationDate?: number;
  modificationDate?: number;
  modifiedBy?: string;
  version?: string;
  publicationDate?: number;
  status?: NodeStatus;
  maxVersionsToKeep?: number;
  favorite?: boolean;
  publicationStatus?: string;
  translations?: Translation[];
}

export interface Node {
  id?: string;
  parentCode?: string;
  parentCodeOrigin?: string;
  name?: string;
  code?: string;
  slug?: string;
  environmentCode?: string;
  description?: string;
  defaultLanguage?: string;
  type?: string;
  subNodes?: string[];
  contents?: ContentNode[];
  tags?: string[];
  values?: Value[];
  roles?: string[];
  rules?: Rule[];
  languages?: string[];
  creationDate?: number;
  modificationDate?: number;
  modifiedBy?: string;
  version?: string;
  publicationDate?: number;
  status?: NodeStatus;
  maxVersionsToKeep?: number;
  favorite?: boolean;
  publicationStatus?: string;
  translations?: Translation[];
}

export interface Language {
  id?: string;
  name?: string;
  code?: string;
  urlIcon?: string;
  description?: string;
}

export interface Feedback {
  id?: string;
  contentCode?: string;
  createdDate?: number;
  modifiedDate?: number;
  evaluation?: number;
  message?: string;
  userId?: string;
  verified?: boolean;
}

export interface Environment {
  id?: string;
  description?: string;
  code?: string;
  name?: string;
  nodeCode?: string;
}

export interface Data {
  id?: string;
  contentNodeCode?: string;
  creationDate?: number;
  modificationDate?: number;
  dataType?: string;
  name?: string;
  user?: string;
  key?: string;
  value?: string;
}

export interface ContentNodePayload {
  code?: string;
  content?: string;
  status?: string;
}

export interface ContentDisplay {
  id?: string;
  contentCode?: string;
  displays?: number;
}

export interface ContentClick {
  id?: string;
  contentCode?: string;
  clicks?: number;
}

export interface AccessRole {
  id?: string;
  name?: string;
  description?: string;
  code?: string;
}

export interface UserLogin {
  email?: string;
  password?: string;
}

export interface AuthResponse {
  token?: string;
}

export interface TreeNode {
  name?: string;
  code?: string;
  type?: string;
  value?: string;
  children?: TreeNode[];
  leaf?: boolean;
}

export interface LockInfo {
  owner?: string;
  mine?: boolean;
  locked?: boolean;
  resourceCode?: string;
}

export interface Chart {
  name?: string;
  value?: string;
  verified?: boolean;
}

export interface FeedbackCharts {
  contentCode?: string;
  charts?: Chart[];
  verified?: Chart[];
  notVerified?: Chart[];
}

export interface ContentDisplayCharts {
  name?: string;
  value?: string;
}

export interface ContentClickCharts {
  name?: string;
  value?: string;
}

// Configuration
export interface NodifyClientConfig {
  baseUrl: string;
  timeout?: number;
  defaultHeaders?: Record<string, string>;
  authToken?: string;
  onAuthError?: () => Promise<string | null>;
}

// Paramètres
export interface PaginationParams {
  currentPage?: number;
  limit?: number;
}

// Erreurs
export class NodifyError extends Error {
  constructor(
    message: string,
    public status?: number,
    public code?: string
  ) {
    super(message);
    this.name = 'NodifyError';
  }
}