export function nullifyEmptyStrings(data) {
  Object.keys(data).forEach(key => {
    if (data[key] === '') {
      data[key] = null;
    }
  });

  return data;
}
