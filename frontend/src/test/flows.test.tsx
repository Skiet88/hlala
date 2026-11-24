import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderApp } from './renderApp';

describe('tenant flows', () => {
  it('explore shows Site C rooms, and the seen in person filter narrows them', async () => {
    const user = userEvent.setup();
    renderApp('/explore');
    expect(await screen.findByText('R2 200')).toBeInTheDocument();
    const before = screen.getAllByRole('link', { name: /R\d/ }).length;
    expect(before).toBe(6);

    await user.click(screen.getByRole('link', { name: /Filters/ }));
    await user.click(screen.getByRole('switch', { name: 'Only rooms seen in person' }));
    await user.click(screen.getByRole('button', { name: 'Show rooms' }));

    await waitFor(() => expect(screen.getAllByRole('link', { name: /R\d/ })).toHaveLength(4));
    expect(screen.getByRole('link', { name: 'Filters, 1 active' })).toBeInTheDocument();
  });

  it('switching section shows that section', async () => {
    const user = userEvent.setup();
    renderApp('/explore');
    await screen.findByText('R2 200');
    await user.click(screen.getByRole('button', { name: 'Harare' }));
    expect(await screen.findByRole('heading', { name: /Rooms in Harare/ })).toBeInTheDocument();
    await waitFor(() => expect(screen.getAllByRole('link', { name: /R\d/ })).toHaveLength(2));
  });

  it('booking a viewing lands in chat with the booked time', async () => {
    const user = userEvent.setup();
    renderApp('/listings/l-site-c-2/viewing');
    await user.click(await screen.findByRole('button', { name: /Sun\s*22/ }));
    await user.click(screen.getByRole('button', { name: '14:00' }));
    await user.click(screen.getByRole('button', { name: 'Confirm free viewing' }));
    expect(await screen.findByText('Sun 22 November at 14:00')).toBeInTheDocument();
    expect(screen.getByText('Bhut Sizwe')).toBeInTheDocument();
  });

  it('sending a chat message adds it to the conversation', async () => {
    const user = userEvent.setup();
    renderApp('/chat');
    await user.type(await screen.findByLabelText('Message'), 'Can I bring my bicycle?');
    await user.click(screen.getByRole('button', { name: 'Send' }));
    expect(await screen.findByText('Can I bring my bicycle?')).toBeInTheDocument();
  });

  it('the fee stays a placeholder: no price is invented', async () => {
    renderApp('/pay');
    expect(await screen.findByText('[YOUR FEE]')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Pay R4 400 into hold' })).toBeInTheDocument();
  });

  it('move in day refuses to release money until every item is ticked', async () => {
    const user = userEvent.setup();
    renderApp('/move-in');
    await user.click(await screen.findByRole('button', { name: 'I have my keys. Release payment' }));
    expect(await screen.findByRole('alert')).toHaveTextContent(/Tick every item/);

    await user.click(screen.getByRole('checkbox', { name: 'Lease signed by both of us' }));
    await user.click(screen.getByRole('checkbox', { name: 'House rules agreed' }));
    await user.click(screen.getByRole('button', { name: 'I have my keys. Release payment' }));
    expect(await screen.findByRole('heading', { name: 'My home' })).toBeInTheDocument();
  });

  it('move out shows what comes back now, and a dispute freezes only the deduction', async () => {
    const user = userEvent.setup();
    renderApp('/home/move-out');
    expect(await screen.findByRole('link', { name: 'Hand back keys and get R1 850' })).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'I do not agree' }));
    expect(await screen.findByText('Sent to the dispute desk')).toBeInTheDocument();
    expect(screen.getByText(/R350 stays frozen/)).toBeInTheDocument();
  });

  it('a report opens a case with the money frozen', async () => {
    const user = userEvent.setup();
    renderApp('/report');
    await user.click(await screen.findByRole('radio', { name: 'Room is not like the photos' }));
    await user.type(screen.getByLabelText(/Tell us more/), 'The toilet is shared');
    await user.click(screen.getByRole('button', { name: 'Send report' }));
    expect(await screen.findByRole('heading', { name: /Case 0144/ })).toBeInTheDocument();
    expect(screen.getByText('The toilet is shared')).toBeInTheDocument();
    expect(screen.getByText('Nothing moves until this is decided')).toBeInTheDocument();
  });
});

describe('landlord flows', () => {
  it('recording cash for a late room updates the rent book', async () => {
    const user = userEvent.setup();
    renderApp('/landlord/rent-book');
    const late = await screen.findByRole('article', { name: 'Room 3 · Lwazi D.' });
    await user.click(within(late).getByRole('button', { name: 'Record cash paid' }));
    expect(await within(late).findByText('Cash, receipt sent')).toBeInTheDocument();
    expect(screen.getByText('R6 400')).toBeInTheDocument();
  });

  it('accepting a viewing request can be undone', async () => {
    const user = userEvent.setup();
    renderApp('/landlord/requests');
    const sipho = await screen.findByRole('article', { name: 'Sipho M.' });
    await user.click(within(sipho).getByRole('button', { name: 'Accept' }));
    expect(await within(sipho).findByText(/Viewing confirmed/)).toBeInTheDocument();
    await user.click(within(sipho).getByRole('button', { name: 'Undo' }));
    expect(await within(sipho).findByRole('button', { name: 'Accept' })).toBeInTheDocument();
  });

  it('listing a room walks the four steps and publishes it', async () => {
    const user = userEvent.setup();
    renderApp('/list/where');
    await user.click(await screen.findByRole('button', { name: 'Makhaza' }));
    await user.click(screen.getByRole('button', { name: 'Continue to rent' }));

    await user.click(await screen.findByRole('button', { name: 'Continue to photos' }));
    expect(await screen.findByRole('alert')).toHaveTextContent(/Enter the rent/);
    await user.type(screen.getByLabelText('Rent per month'), 'R1 950');
    await user.type(screen.getByLabelText('Deposit'), '1950');
    await user.click(screen.getByRole('button', { name: 'Continue to photos' }));

    await user.click(await screen.findByRole('button', { name: 'Continue to preview' }));
    expect(await screen.findByText(/in Makhaza/)).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Publish my room' }));
    expect(await screen.findByRole('heading', { name: 'Your rooms' })).toBeInTheDocument();
  });
});

describe('trust and living', () => {
  it('the electricity split switches between per room and per person', async () => {
    const user = userEvent.setup();
    renderApp('/home/electricity');
    expect(await screen.findByText('Gets R112.50')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'By number of people' }));
    expect(await screen.findByText('Gets R150')).toBeInTheDocument();
  });

  it('the checker sees the pay placeholder and cannot award the badge early', async () => {
    const user = userEvent.setup();
    renderApp('/checker');
    expect(await screen.findByText('[PAY PER CHECK]')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Submit check, 3 of 6' }));
    expect(await screen.findByRole('alert')).toHaveTextContent(/Every item/);
  });

  it('reviews stay sealed after posting', async () => {
    const user = userEvent.setup();
    renderApp('/home/reviews');
    await user.click(await screen.findByRole('radio', { name: '5 out of 5' }));
    await user.click(screen.getByRole('button', { name: 'Post review' }));
    expect(await screen.findByRole('heading', { name: 'Review sealed' })).toBeInTheDocument();
  });
});
