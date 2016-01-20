function [] = generateSkewedData(n)
%CHI2RND Random arrays from chi-square distribution.
%   n = number of samples

    CHI2D = chi2rnd(4,1,n); % sample from chi2 distribution

    figure;set(gcf,'Color','w');set(gca,'FontSize',14,'LineWidth',2)
    hold on

    nbin=100; % we use nbin bins in the histogram
    [N,X] = hist(CHI2D,nbin);
    N = N./n; % convert histogram frequencies to proportions
    bar(X,N);
    h = findobj(gca,'Type','patch');
    set(h,'FaceColor',[.9 .9 .9],'EdgeColor',[0 0 0])
    set(gca,'Layer','Top')
end