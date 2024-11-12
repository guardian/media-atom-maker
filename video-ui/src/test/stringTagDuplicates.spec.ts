import removeStringTagDuplicates from '../util/removeStringTagDuplicates';

const simpleTag = { id: 'tag', webTitle: 'tag' };

describe('Removing string tag duplicates', () => {
  it('does not trim when input is empty', () => {
    expect(removeStringTagDuplicates(simpleTag, [])).toEqual([]);
  });

  it('does not trim duplicates when matches do not exist', () => {
    const savedValue = ['text'];

    expect(removeStringTagDuplicates(simpleTag, savedValue)).toEqual(
      savedValue
    );
  });

  it('does not trim duplicates from the middle of values', () => {
    const savedValue = ['tag', 'text'];
    expect(removeStringTagDuplicates(simpleTag, savedValue)).toEqual(
      savedValue
    );
  });

  it('does not trim duplicates from a tag', () => {
    const savedValue = ['tag', simpleTag];
    expect(removeStringTagDuplicates(simpleTag, savedValue)).toEqual(
      savedValue
    );
  });

  it('trims one duplicate', () => {
    const savedValue = ['tag'];
    expect(removeStringTagDuplicates(simpleTag, savedValue)).toEqual([]);
  });

  it('is case insensitive on saved values', () => {
    const savedValue = ['TAG'];
    expect(removeStringTagDuplicates(simpleTag, savedValue)).toEqual([]);
  });

  it('is case insensitive on tags', () => {
    const savedValue = ['tag'];
    const tagWithCapitals = { id: 'tag', webTitle: 'TAG' };
    expect(removeStringTagDuplicates(tagWithCapitals, savedValue)).toEqual([]);
  });

  it('trims multiple duplicates', () => {
    const tagWithLongName = { id: 'tag', webTitle: 'this is a tag' };
    const savedValue = ['this', 'was', 'a', 'tag'];
    expect(removeStringTagDuplicates(tagWithLongName, savedValue)).toEqual([
      'this',
      'was'
    ]);
  });
});
