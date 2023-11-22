export const  canonicalVideoPageExists = (usages: {totalVideoPages: number}) => {
  return usages.totalVideoPages > 0;
};

