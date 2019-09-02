import getProductionOffice from '../util/getProductionOffice';

const assumedProdOffice = getProductionOffice(); // guess default by user's timezone

export const defaultWorkflowStatusData = () => {
  return { prodOffice: assumedProdOffice };
};
