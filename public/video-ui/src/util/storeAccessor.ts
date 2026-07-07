import { Store } from './setupStore';

let savedStore: Store;

export function setStore(store: Store) {
  savedStore = store;
}

export function getStore(): Store {
  return savedStore;
}
